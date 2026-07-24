package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isNullableString
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isStringClassType
import org.jetbrains.kotlin.ir.util.allParameters
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.getOwnerIfBound
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.utils.zipIfSizesAreEqual

@OptIn(UnsafeDuringIrConstructionAPI::class)
class IrTemplateProcessorFacadeUseActualizer(override val context: IrPluginContext) : WithContext, IrElementTransformerVoidWithContext() {
    private val stringTemplateClass: IrClass by lazy {
        finder.findClass(Symbols.StringTemplate)!!.owner
    }

    private val simpleStringTemplateConstructor: IrConstructorSymbol by lazy {
        finder.findConstructors(Symbols.SimpleStringTemplate).single()
    }

    private val listOfVariants: Collection<IrSimpleFunctionSymbol> by lazy {
        finder.findFunctions(Symbols.listOf)
    }

    private val listOfVararg: IrSimpleFunctionSymbol by lazy {
        listOfVariants.single { symbol ->
            val func = symbol.getOwnerIfBound()
            func?.parameters?.size == 1 && func.parameters[0].varargElementType != null }
    }

    private val listOfEmpty: IrSimpleFunctionSymbol by lazy {
        listOfVariants.single { it.getOwnerIfBound()?.parameters?.isEmpty() == true }
    }

    private fun IrType.isStringTemplate(): Boolean {
        return classOrNull == stringTemplateClass.symbol
    }

    private fun findOriginalFunction(facade: IrFunction): IrSimpleFunction? {
        val id = facade.callableId
        val symbol = finder.findFunctions(id).find {
            val func = it.owner
            if (!func.hasAnnotation(Symbols.TemplateProcessor)) return@find false

            val params = func.allParameters.zipIfSizesAreEqual(facade.allParameters) ?: return@find false
            params.forEach { (curr, facade) ->
                if (curr.type.isStringTemplate()) {
                    val nullable = curr.type.isNullable()
                    if (nullable && !facade.type.isNullableString()) return@find false
                    if (!nullable && !facade.type.isString()) return@find false
                } else {
                    if (curr.type != facade.type) return@find false
                }
            }

            true
        }
        return symbol?.owner
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration.hasAnnotation(Symbols.FacadeInterpolatorCall) &&
            declaration.name.asString().endsWith("\$default")) {
            declaration.body?.let { body ->
                body.transformChildrenVoid()
                fixUndefinedOffsetsDeep(body)
            }
        }
        return super.visitFunctionNew(declaration)
    }

    private fun fixUndefinedOffsetsDeep(element: IrElement) {
        if (element.startOffset == UNDEFINED_OFFSET) {
            element.startOffset = 0
        }
        if (element.endOffset == UNDEFINED_OFFSET) {
            element.endOffset = 0
        }
        element.acceptChildren(object : IrVisitor<Unit, Nothing?>() {
            override fun visitElement(element: IrElement, data: Nothing?) {
                fixUndefinedOffsetsDeep(element)
            }
        }, null)
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        val function = expression.symbol.owner

        if (function.hasAnnotation(Symbols.FacadeInterpolatorCall)) {
            val original = findOriginalFunction(function)
            if (original != null) {
                return replaceWithOriginalCall(expression, original)
            }
        }

        return super.visitFunctionAccess(expression)
    }

    private fun replaceWithOriginalCall(
        expression: IrFunctionAccessExpression,
        originalFunction: IrSimpleFunction
    ): IrExpression {
        val facadeFunction = expression.symbol.owner
        val builder = expression.getBuilder()

        val newArgs = mutableMapOf<Int, IrExpression>()

        val facadeParams = facadeFunction.parameters
        val origParams = originalFunction.parameters

        for (i in facadeParams.indices) {
            val arg = expression.arguments[i] ?: continue
            val facadeParam = facadeParams[i]
            val origParam = origParams[i]

            if ((facadeParam.type.isString() || facadeParam.type.isNullableString()) && origParam.type.isStringTemplate()) {
                newArgs[i] = createStringTemplateFromString(arg, builder)
            } else {
                newArgs[i] = arg
            }
        }

        val newCall = builder.irCall(originalFunction.symbol)

        for (i in newCall.typeArguments.indices) {
            val origTypeArg = expression.typeArguments.getOrNull(i)
            if (origTypeArg != null) {
                newCall.typeArguments[i] = origTypeArg
            }
        }

        for ((index, arg) in newArgs) {
            newCall.arguments[index] = arg
        }

        return newCall
    }

    private fun createStringTemplateFromString(stringExpr: IrExpression, builder: DeclarationIrBuilder): IrExpression {
        val (surroundings, holes) = when (stringExpr) {
            is IrConst -> {
                val value = stringExpr.value as? String ?: ""
                listOf(value) to emptyList<IrExpression>()
            }
            is IrStringConcatenation -> {
                val surroundings = mutableListOf<String>()
                val holes = mutableListOf<IrExpression>()
                var current = StringBuilder()
                for (arg in stringExpr.arguments) {
                    val c = arg as? IrConst
                    val str = c?.value as? String
                    if (str != null) {
                        current.append(str)
                    } else {
                        surroundings.add(current.toString())
                        current = StringBuilder()
                        holes.add(arg)
                    }
                }
                surroundings.add(current.toString())
                surroundings to holes
            }
            else -> {
                throw UnsupportedOperationException(
                    "Only strings literals and string templates are accepted as StringTemplate arguments but got " +
                            stringExpr::class.simpleName
                )
            }
        }

        return createSimpleStringTemplateCall(surroundings, holes, builder)
    }

    private fun createSimpleStringTemplateCall(
        surroundings: List<String>,
        holes: List<IrExpression>,
        builder: DeclarationIrBuilder
    ): IrExpression {
        val surroundingsList = builder.irListCall(builtIns.stringType, surroundings.map(builder::irString))
        val holesList = builder.irListCall(builtIns.anyType, holes)

        val constructorCall = builder.irCall(simpleStringTemplateConstructor)
        constructorCall.arguments[0] = surroundingsList
        constructorCall.arguments[1] = holesList

        return constructorCall
    }

    private fun DeclarationIrBuilder.irListCall(elementType: IrType, elements: List<IrExpression>): IrExpression {
        if (elements.isEmpty()) {
            val call = this.irCall(listOfEmpty)
            call.typeArguments[0] = elementType
            return call
        }

        val vararg = irVararg(elementType, elements)

        val call = this.irCall(listOfVararg)
        call.typeArguments[0] = elementType
        call.arguments[0] = vararg

        return call
    }
}
