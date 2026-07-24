package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirStringConcatenationCall
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.builder.FirStringConcatenationCallBuilder
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.types.ConstantValueKind

class RightStringTemplateUseChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers = object : DeclarationCheckers() {
        override val functionCheckers: Set<FirFunctionChecker>
            get() = setOf(DeclarationChecker)
    }

    override val expressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers: Set<FirExpressionChecker<FirFunctionCall>>
            get() = setOf(FunctionCallChecker(this@RightStringTemplateUseChecker.session))
    }

    private val predicate = LookupPredicate.create { annotatedOrUnder(Symbols.TemplateProcessor.asSingleFqName()) }
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    object DeclarationChecker : FirFunctionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirFunction) {
            val isInterpolator = declaration.isInterpolator()
            val markedInterpolator = declaration.hasAnnotation(Symbols.TemplateProcessor, session)

            if (!isInterpolator && !markedInterpolator) return

            if (isInterpolator && !markedInterpolator) {
                reporter.reportOn(declaration.source, ErrorsCustomStringTemplate.NOT_A_TEMPLATE_PROCESSOR)
                return
            }

            if (!isInterpolator) {
                reporter.reportOn(declaration.source, ErrorsCustomStringTemplate.NOTHING_TO_INTERPOLATE)
                return
            }

            if (declaration.isLocal) {
                reporter.reportOn(declaration.source, ErrorsCustomStringTemplate.LOCAL_TEMPLATE_PROCESSOR)
                return
            }

            for (param in declaration.valueParameters) {
                if (param.returnTypeRef.isNullableStringTemplate()) {
                    reporter.reportOn(param.source, ErrorsCustomStringTemplate.NULLABLE_STRING_TEMPLATE)
                }
            }

            for (param in declaration.contextParameters) {
                if (param.returnTypeRef.isNullableStringTemplate()) {
                    reporter.reportOn(param.source, ErrorsCustomStringTemplate.NULLABLE_STRING_TEMPLATE)
                }
            }

            val receiver = declaration.receiverParameter
            if (receiver != null && receiver.typeRef.isNullableStringTemplate()) {
                reporter.reportOn(receiver.source, ErrorsCustomStringTemplate.NULLABLE_STRING_TEMPLATE)
            }

            for (param in declaration.valueParameters + declaration.contextParameters) {
                if (param.defaultValue != null) {
                    reporter.reportOn(param.source, ErrorsCustomStringTemplate.DEFAULT_PARAM_IN_TEMPLATE_PROCESSOR)
                }
            }

            if (declaration.status.isOverride) {
                checkFacadeOverride(declaration)
            }
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        private fun checkFacadeOverride(declaration: FirFunction) {
            reporter.reportOn(declaration.source, ErrorsCustomStringTemplate.FACADE_OVERRIDE)
        }
    }

    inner class FunctionCallChecker(private val session: FirSession) : FirExpressionChecker<FirFunctionCall>(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirFunctionCall) {
            val functionSymbol = expression.calleeReference.toResolvedFunctionSymbol() ?: return
            if (!functionSymbol.hasAnnotation(Symbols.FacadeInterpolatorCall, session)) return

            val originalFunction = findOriginalFunction(functionSymbol) ?: return
            val facadeParams = functionSymbol.contextParameterSymbols + functionSymbol.valueParameterSymbols
            val origParams = originalFunction.contextParameterSymbols + originalFunction.valueParameterSymbols

            fun reportIfNotTemplate(arg: FirExpression) {
                if (arg is FirLiteralExpression && arg.kind == ConstantValueKind.String) return
                if (arg is FirStringConcatenationCall) {
                    if (arg.isFoldedStrings) reporter.reportOn(arg.source, ErrorsCustomStringTemplate.NOT_A_LITERAL_OR_TEMPLATE)
                    return
                }
                reporter.reportOn(arg.source, ErrorsCustomStringTemplate.NOT_A_LITERAL_OR_TEMPLATE)
            }

            fun checkArguments(argument: FirExpression) {
                if (argument is FirVarargArgumentsExpression) {
                    for (inner in argument.arguments) {
                        reportIfNotTemplate(inner)
                    }
                } else {
                    reportIfNotTemplate(argument)
                }
            }

            val arguments = expression.contextArguments + expression.argumentList.arguments
            for (i in facadeParams.indices) {
                val origParam = origParams.getOrNull(i) ?: continue
                if (!origParam.resolvedReturnTypeRef.isStringTemplate()) continue

                val argument = arguments.getOrNull(i) ?: continue
                checkArguments(argument)
            }

            if (originalFunction.resolvedReceiverTypeRef?.isStringTemplate() == true) {
                val argument = expression.extensionReceiver ?: expression.dispatchReceiver
                if (argument != null) reportIfNotTemplate(argument)
            }
        }

        private fun findOriginalFunction(facadeSymbol: FirFunctionSymbol<*>): FirFunctionSymbol<*>? {
            return session.predicateBasedProvider.getSymbolsByPredicate(predicate)
                .filterIsInstance<FirFunctionSymbol<*>>()
                .firstOrNull { orig ->
                    // TODO: check parameters instead of isInterpolator
                    orig.callableId == facadeSymbol.callableId && orig.isInterpolator()
                }
        }
    }
}
