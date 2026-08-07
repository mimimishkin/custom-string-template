package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.impl.FirSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

class FirTemplateProcessorFacadeGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    object TemplateProcessorFacade : GeneratedDeclarationKey()

    companion object {
        private val predicate = LookupPredicate.create { annotatedOrUnder(Symbols.TemplateProcessor.asSingleFqName()) }
        private val facadePredicate = LookupPredicate.create { annotatedOrUnder(Symbols.FacadeInterpolatorCall.asSingleFqName()) }
    }
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
        register(facadePredicate)
    }

    private val processorByClasses: Map<ClassId?, MutableMap<CallableId, MutableList<FirCallableSymbol<*>>>> by lazy {
        session.predicateBasedProvider.getSymbolsByPredicate(predicate)
            .asSequence()
            .filterIsInstance<FirCallableSymbol<*>>()
            .filter { it.isInterpolator() }
            .filter { !it.isActual }
            .filter { it.callableId != null }
            .groupBy { it.callableId!!.classId }
            .mapValues { (_, symbol) ->
                symbol.groupByTo(mutableMapOf()) { it.callableId!! }
            }
    }

    /**
     * Return names of the class-level @TemplateProcessor functions.
     */
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        // we will generate a sibling functions
        val thisClassProcessors = processorByClasses[classSymbol.classId] ?: return emptySet()
        return thisClassProcessors.mapTo(mutableSetOf()) { (id, _) -> id.callableName }
    }

    /**
     * Return names of the top-level @TemplateProcessor functions.
     */
    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        // we will generate a sibling functions
        val topLevelProcessors = processorByClasses[null] ?: return emptySet()
        return topLevelProcessors.mapTo(mutableSetOf()) { (id, _) -> id }
    }

    private val annotationFacadeInterpolatorCall: FirClassifierSymbol<*> by lazy {
        Symbols.FacadeInterpolatorCall.toSymbol(session)!!
    }

    private val funInterpolationDisabled by lazy {
        session.symbolProvider
            .getTopLevelFunctionSymbols(Symbols.interpolationDisabled.packageName, Symbols.interpolationDisabled.callableName)
            .single()
    }

    private fun originalBy(callableId: CallableId): List<FirCallableSymbol<*>> {
        val classSymbols = processorByClasses[callableId.classId]
        return classSymbols?.remove(callableId) ?: emptyList()
    }

    /**
     * True when the user supplied their own @FacadeInterpolatorCall for this callable,
     * in which case the plugin must not generate a duplicate.
     */
    private val userFacadeIds: Set<CallableId> by lazy {
        session.predicateBasedProvider.getSymbolsByPredicate(facadePredicate)
            .filterIsInstance<FirCallableSymbol<*>>()
            .mapNotNull { it.callableId }
            .toSet()
    }

    private fun hasUserFacade(callableId: CallableId): Boolean = callableId in userFacadeIds

    /**
     * Generate sibling stub functions.
     */
    @OptIn(SymbolInternals::class)
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        if (hasUserFacade(callableId)) return emptyList()
        val interfaceMember = context?.owner?.classKind == ClassKind.INTERFACE
        return originalBy(callableId)
            .filterIsInstance<FirNamedFunctionSymbol>()
            .map { original ->
                val sibling = buildNamedFunctionCopy(original.fir) {
                    configFacade(callableId, interfaceMember)
                    dispatchReceiverType = context?.owner?.defaultType()
                }
                sibling.symbol
            }
    }

    /**
     * Generate sibling stub properties.
     */
    @OptIn(SymbolInternals::class)
    override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
        if (hasUserFacade(callableId)) return emptyList()
        val interfaceMember = context?.owner?.classKind == ClassKind.INTERFACE
        return originalBy(callableId)
            .filterIsInstance<FirPropertyAccessorSymbol>()
            .map { original ->
                val sibling = buildPropertyCopy(original.propertySymbol.fir) {
                    configFacade(callableId, interfaceMember)
                    dispatchReceiverType = context?.owner?.defaultType()
                }
                sibling.symbol
            }
    }

    private fun FirDeclarationBuilder.configFacade(callableId: CallableId, interfaceMember: Boolean): Unit = with(session.typeContext) {
        val builder = this@configFacade
        val isProperty = builder is FirPropertyBuilder
        val isFunction = builder is FirNamedFunctionBuilder
        require(isFunction || isProperty)

        val thisSymbol = if (isFunction) {
            symbol = FirNamedFunctionSymbol(callableId)
            symbol
        } else {
            builder as FirPropertyBuilder
            symbol = FirRegularPropertySymbol(callableId)
            symbol
        }

        resolvePhase = FirResolvePhase.BODY_RESOLVE
        origin = FirDeclarationOrigin.Plugin(TemplateProcessorFacade)
        moduleData = session.moduleData
        source = null

        fun siblingModality(currentModality: Modality?): Modality = when (currentModality) {
            Modality.ABSTRACT, Modality.OPEN -> Modality.OPEN
            else -> if (interfaceMember) Modality.OPEN else Modality.FINAL
        }
        if (isFunction) {
            val currentStatus = status
            status = FirResolvedDeclarationStatusImpl(
                currentStatus.visibility.takeIf { it != Visibilities.Unknown } ?: Visibilities.Public,
                siblingModality(currentStatus.modality),
                EffectiveVisibility.Public
            )
        } else if (isProperty) {
            val currentStatus = status
            status = FirResolvedDeclarationStatusImpl(
                currentStatus.visibility.takeIf { it != Visibilities.Unknown } ?: Visibilities.Public,
                siblingModality(currentStatus.modality),
                EffectiveVisibility.Public
            )
        }

        val stringType = session.builtinTypes.stringType.coneType

        fun FirTypeRef.toNullableString(): ConeKotlinType =
            stringType.withNullability(coneType.isMarkedNullable) as ConeKotlinType

        fun FirReceiverParameter.toStringIfTemplate(): FirReceiverParameter =
            if (typeRef.isStringTemplate()) buildReceiverParameterCopy(this) {
                origin = FirDeclarationOrigin.Plugin(TemplateProcessorFacade)
                source = null
                symbol = FirReceiverParameterSymbol()
                typeRef = this@toStringIfTemplate.typeRef.withReplacedConeType(
                    this@toStringIfTemplate.typeRef.toNullableString()
                )
            } else this

        fun FirValueParameter.toStringIfTemplate(): FirValueParameter =
            if (returnTypeRef.isStringTemplate()) buildValueParameterCopy(this) {
                origin = FirDeclarationOrigin.Plugin(TemplateProcessorFacade)
                source = null
                symbol = FirValueParameterSymbol()
                returnTypeRef = if (this@toStringIfTemplate.isVararg) {
                    val arrayType = this@toStringIfTemplate.returnTypeRef.coneType as ConeClassLikeType
                    val elementType = arrayType.varargElementType()
                    val newElementType = stringType.withNullability(elementType.isMarkedNullable) as ConeKotlinType
                    this@toStringIfTemplate.returnTypeRef.withReplacedConeType(
                        arrayType.withArguments(arrayOf(ConeKotlinTypeProjectionOut(newElementType)))
                    )
                } else {
                    this@toStringIfTemplate.returnTypeRef.withReplacedConeType(
                        this@toStringIfTemplate.returnTypeRef.toNullableString()
                    )
                }
            } else this

        when (builder) {
            is FirNamedFunctionBuilder -> {
                builder.receiverParameter = builder.receiverParameter?.toStringIfTemplate()
                builder.valueParameters.replaceAll { it.toStringIfTemplate() }
                builder.contextParameters.replaceAll { it.toStringIfTemplate() }
            }
            is FirPropertyBuilder -> {
                builder.receiverParameter = builder.receiverParameter?.toStringIfTemplate()
                builder.contextParameters.replaceAll { it.toStringIfTemplate() }
            }
        }

        annotations.replaceAll { annotation ->
            if (annotation.toAnnotationClassId(session) == Symbols.TemplateProcessor) {
                buildAnnotationCall {
                    containingDeclarationSymbol = thisSymbol
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = annotationFacadeInterpolatorCall.constructType()
                    }
                    calleeReference = buildResolvedNamedReference {
                        name = Symbols.FacadeInterpolatorCall.shortClassName
                        resolvedSymbol = annotationFacadeInterpolatorCall
                    }
                }
            } else {
                annotation
            }
        }

        val removableAnnotations = setOf(Symbols.ObjCName, Symbols.CName, Symbols.JsExport)
        annotations.removeAll { it.toAnnotationClassId(session) in removableAnnotations }

        val hidingAnnotations = listOf(Symbols.JvmSynthetic, Symbols.HideFromObjC, Symbols.JsExportIgnore)
        for (classId in hidingAnnotations) {
            val symbol = classId.toSymbol(session) ?: continue
            annotations.add(buildAnnotationCall {
                containingDeclarationSymbol = thisSymbol
                annotationTypeRef = buildResolvedTypeRef {
                    coneType = symbol.constructType()
                }
                calleeReference = buildResolvedNamedReference {
                    name = classId.shortClassName
                    resolvedSymbol = symbol
                }
            })
        }

        val newBody = FirSingleExpressionBlock(
            buildFunctionCall {
                coneTypeOrNull = session.builtinTypes.nothingType.coneType
                calleeReference = buildResolvedNamedReference {
                    name = Symbols.interpolationDisabled.callableName
                    resolvedSymbol = funInterpolationDisabled
                }
            }
        )

        if (isFunction) {
            body = newBody
        } else if (isProperty) {
            fun accessor(): FirPropertyAccessor = buildPropertyAccessor {
                resolvePhase = FirResolvePhase.BODY_RESOLVE
                origin = FirDeclarationOrigin.Plugin(TemplateProcessorFacade)
                body = newBody
            }
            builder.getter = accessor()
            builder.setter = null
        }
    }
}