package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildReturnExpression
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class FirTemplateProcessorFacadeGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    object TemplateProcessorFacade : GeneratedDeclarationKey()

    val predicate = LookupPredicate.create { annotatedOrUnder(Symbols.TemplateProcessor.asSingleFqName()) }
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    private val processorByClasses by lazy {
        val provider = session.predicateBasedProvider
        val predicate = LookupPredicate.create { annotated(Symbols.TemplateProcessor.asSingleFqName()) }
        val allTemplates = provider.getSymbolsByPredicate(predicate).filterIsInstance<FirFunctionSymbol<*>>()
        allTemplates.groupBy { it.callableId.classId }
    }

    private fun getOriginal(callableId: CallableId): List<FirFunctionSymbol<*>> {
        return processorByClasses[callableId.classId]!!.filter { it.name == callableId }
    }

    /**
     * Return names of the class-level @TemplateProcessor functions.
     */
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        // we will generate a sibling functions
        val thisClassProcessors = processorByClasses[classSymbol.classId] ?: return emptySet()
        return thisClassProcessors.mapTo(mutableSetOf()) { it.name }
    }

    /**
     * Return names of the top-level @TemplateProcessor functions.
     */
    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        // we will generate a sibling functions
        val topLevelProcessors = processorByClasses[null] ?: return emptySet()
        return topLevelProcessors.mapTo(mutableSetOf()) { it.callableId }
    }

    private val annotationActualInterpolatorCall: FirClassifierSymbol<*> by lazy {
        Symbols.FacadeInterpolatorCall.toSymbol(session)!!
    }

    private val funInterpolationDisabled by lazy {
        session.symbolProvider
            .getTopLevelFunctionSymbols(Symbols.interpolationDisabled.packageName, Symbols.interpolationDisabled.callableName)
            .single()
    }

    private fun originalBy(callableId: CallableId): List<FirFunctionSymbol<*>> {
        return processorByClasses[callableId.classId]!!.filter { it.name == callableId.callableName }
    }

    /**
     * Generate sibling stub functions.
     */
    @OptIn(SymbolInternals::class)
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> = with(session.typeContext) {
        return originalBy(callableId).filterIsInstance<FirNamedFunctionSymbol>().map { original ->
            val sibling = buildNamedFunctionCopy(original.fir) {
                symbol = FirNamedFunctionSymbol(callableId)
                configFacade()
            }
            sibling.symbol
        }
    }

    /**
     * Generate sibling stub properties.
     */
    @OptIn(SymbolInternals::class)
    override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
        return originalBy(callableId).filterIsInstance<FirPropertyAccessorSymbol>().map { original ->
            val sibling = buildPropertyCopy(original.propertySymbol.fir) {
                symbol = FirRegularPropertySymbol(callableId)
                configFacade()
            }
            sibling.symbol
        }
    }

    private fun FirDeclarationBuilder.configFacade() = with(session.typeContext) {
        val builder = this@configFacade
        val isProperty = builder is FirPropertyBuilder
        val isFunction = builder is FirNamedFunctionBuilder
        require(isFunction || isProperty)

        // set up resolve phase that complier expects
        resolvePhase = FirResolvePhase.BODY_RESOLVE

        // set up origin
        origin = FirDeclarationOrigin.Plugin(TemplateProcessorFacade)

        // prepare string cone type
        val stringType = session.builtinTypes.stringType.coneType
        fun FirReceiverParameter.toStringParam(): FirReceiverParameter {
            // deep copy value parameter but replace type ref with string type
            return buildReceiverParameterCopy(this) {
                symbol = this@toStringParam.symbol
                typeRef = this@toStringParam.typeRef.withReplacedConeType(
                    stringType.withNullability(this@toStringParam.typeRef.coneType.isMarkedNullable) as ConeKotlinType
                )
            }
        }
        fun FirValueParameter.toStringParam(): FirValueParameter {
            // deep copy value parameter but replace type ref with string type
            return buildValueParameterCopy(this) {
                symbol = this@toStringParam.symbol
                returnTypeRef = this@toStringParam.returnTypeRef.withReplacedConeType(
                    stringType.withNullability(this@toStringParam.returnTypeRef.coneType.isMarkedNullable) as ConeKotlinType
                )
            }
        }

        // replace [StringTemplate]s with [String]s
        val receiverParameter = (builder as? FirPropertyBuilder)?.receiverParameter ?: (builder as FirNamedFunctionBuilder).receiverParameter
        if (receiverParameter?.typeRef?.isStringTemplate() == true) {
            // deep copy receiver parameter but replace type ref with string type
            val stringParam = receiverParameter.toStringParam()
            if (builder is FirPropertyBuilder) {
                builder.receiverParameter = stringParam
            } else if (builder is FirNamedFunctionBuilder) {
                builder.receiverParameter = stringParam
            }
        }

        val valueParameters = (builder as? FirNamedFunctionBuilder)?.valueParameters ?: mutableListOf()
        valueParameters.forEachIndexed { i, parameter ->
            if (parameter.returnTypeRef.isStringTemplate()) {
                valueParameters[i] = parameter.toStringParam()
            }
        }
        val contextParameters = (builder as? FirPropertyBuilder)?.contextParameters ?: (builder as FirNamedFunctionBuilder).contextParameters
        contextParameters.forEachIndexed { i, parameter ->
            if (parameter.returnTypeRef.isStringTemplate()) {
                contextParameters[i] = parameter.toStringParam()
            }
        }

        // TODO: check @FacadeInterpolatorCall not to be used together with @TemplateProcessor

        // replace @TemplateProcessor with @FacadeInterpolatorCall
        annotations.forEachIndexed { i, annotation ->
            if (annotation.toAnnotationClassId(session) == Symbols.TemplateProcessor) {
                annotations[i] = buildAnnotationCall {
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = annotationActualInterpolatorCall.constructType()
                    }
                    calleeReference = buildResolvedNamedReference {
                        name = Symbols.FacadeInterpolatorCall.shortClassName
                        resolvedSymbol = annotationActualInterpolatorCall
                    }
                }
            }
        }

        // TODO: remove export annotations, restrict export

        // always throw an error in body
        if (builder is FirNamedFunctionBuilder) {
            // replace body
            body = blockReturnInterpolationDisabled()
        } else if (builder is FirPropertyBuilder) {
            fun accessor(): FirPropertyAccessor = buildPropertyAccessor {
                // set up resolve phase that complier expects
                resolvePhase = FirResolvePhase.BODY_RESOLVE

                // set up origin
                origin = FirDeclarationOrigin.Plugin(TemplateProcessorFacade)

                body = blockReturnInterpolationDisabled()
            }

            // replace bodies
            builder.getter = accessor()
            if (builder.isVar) builder.setter = accessor()
        }
    }
    private fun blockReturnInterpolationDisabled(): FirBlock = buildBlock {
        statements += buildReturnExpression {
            result = buildFunctionCall {
                calleeReference = buildResolvedNamedReference {
                    name = Symbols.interpolationDisabled.callableName
                    resolvedSymbol = funInterpolationDisabled
                }
            }
        }
    }
}