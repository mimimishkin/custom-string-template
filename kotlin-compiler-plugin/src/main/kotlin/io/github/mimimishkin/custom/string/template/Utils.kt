package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.fir.types.varargElementType
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

interface WithContext {
    val context: IrPluginContext

    val builtIns get() = context.irBuiltIns
    val reporter get() = context.diagnosticReporter
    val factory get() = context.irFactory
    val finder get() = context.finderForBuiltins()

    fun IrDeclaration.getBuilder() = DeclarationIrBuilder(context, symbol, startOffset, endOffset)

    fun IrDeclarationReference.getBuilder() = DeclarationIrBuilder(context, symbol, startOffset, endOffset)
}

//val KClass<*>.classId: ClassId?
//    get() = qualifiedName?.let { ClassId.fromString(it) } ?: simpleName?.let { ClassId.fromString(it, isLocal = true) }

fun String.fqn() = FqName(this)

fun String.ident() = Name.identifier(this)

val FqName.Companion.LOCAL get() = CallableId.PACKAGE_FQ_NAME_FOR_LOCAL

fun FqName.topClassId(name: String) = ClassId(this, name.ident())

fun localClassId(name: String) = ClassId(FqName.LOCAL, FqName.topLevel(name.ident()), isLocal = true)

fun FqName.topCallableId(name: String) = CallableId(this, name.ident())

context(context: CheckerContext)
inline val session get() = context.session

fun FirTypeRef.isStringTemplate(): Boolean {
    return coneType.varargElementType().classId == Symbols.StringTemplate
}

fun FirTypeRef.isNullableStringTemplate(): Boolean {
    val elementType = coneType.varargElementType()
    return elementType.classId == Symbols.StringTemplate && elementType.isMarkedNullable
}

fun FirValueParameter.isStringTemplate(): Boolean = returnTypeRef.isStringTemplate()

fun FirFunction.isInterpolator(): Boolean =
    valueParameters.any { it.returnTypeRef.isStringTemplate() } ||
            contextParameters.any { it.returnTypeRef.isStringTemplate() } ||
            receiverParameter?.typeRef?.isStringTemplate() == true

fun FirFunctionSymbol<*>.isInterpolator(): Boolean =
    valueParameterSymbols.any { it.resolvedReturnTypeRef.isStringTemplate() } ||
            contextParameterSymbols.any { it.resolvedReturnTypeRef.isStringTemplate() } ||
            receiverParameterSymbol?.calculateResolvedTypeRef()?.isStringTemplate() == true

fun FirPropertySymbol.isInterpolator(): Boolean =
    resolvedReturnTypeRef.isStringTemplate() ||
            receiverParameterSymbol?.calculateResolvedTypeRef()?.isStringTemplate() == true

fun FirCallableSymbol<*>.isInterpolator(): Boolean = when (this) {
    is FirFunctionSymbol<*> -> this.isInterpolator()
    is FirPropertySymbol -> this.isInterpolator()
    else -> false
}

fun FirProperty.isInterpolator(): Boolean =
    returnTypeRef.isStringTemplate() ||
            receiverParameter?.typeRef?.isStringTemplate() == true