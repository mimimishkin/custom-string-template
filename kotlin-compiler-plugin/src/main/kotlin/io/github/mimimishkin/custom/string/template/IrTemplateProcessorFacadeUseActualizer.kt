package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrCallableReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression

class IrTemplateProcessorFacadeUseActualizer(override val context: IrPluginContext) : WithContext, IrElementTransformerVoidWithContext() {
//    private val interpolationDisabled by lazy { finder.findFunctions(Symbols.interpolationDisabled).single() }
//    private val FacadeInterpolatorCall by lazy { finder.findConstructors(Symbols.FacadeInterpolatorCall).single() }
//    private val OptIn by lazy { finder.findConstructors(Symbols.OptIn).single() }
//    private val listOfFun by lazy { finder.findFunctions(Symbols.listOf) }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        // TODO: check if it's a call to the facade function and if it is, replace it with a call to the actual function
        return super.visitFunctionAccess(expression)
    }

    override fun visitCallableReference(expression: IrCallableReference<*>): IrExpression {
        // TODO in future: forbid getting @TemplateProcessor functions references or trace them and apply
        //  the same rules
        return super.visitCallableReference(expression)
    }
}