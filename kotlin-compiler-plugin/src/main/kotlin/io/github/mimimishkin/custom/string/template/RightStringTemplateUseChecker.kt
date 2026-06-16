package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol

class RightStringTemplateUseChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers = object : DeclarationCheckers() {
        override val functionCheckers: Set<FirFunctionChecker>
            get() = setOf(
                Checker
            )
    }

    object Checker : FirFunctionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirFunction) {
            val isInterpolator = declaration.valueParameters.any { it.returnTypeRef.isStringTemplate() } ||
                    declaration.contextParameters.any { it.returnTypeRef.isStringTemplate() } ||
                    declaration.receiverParameter?.typeRef?.isStringTemplate() == true

            val markedInterpolator = declaration.hasAnnotation(Symbols.TemplateProcessor, session)

            // check if we're interested
            if (!isInterpolator && !markedInterpolator) return

            // check if interpolator is not annotated
            if (isInterpolator && !markedInterpolator) {
                reporter.reportOn(declaration.source, ErrorsCustomStringTemplate.NOT_A_TEMPLATE_PROCESSOR)
                return
            }

            // here markedInterpolator == true
            // check if interpolator has nothing to interpolate
            if (!isInterpolator) {
                reporter.reportOn(declaration.source, ErrorsCustomStringTemplate.NOTHING_TO_INTERPOLATE)
                return
            }

            declaration.isLocal
        }
    }

}