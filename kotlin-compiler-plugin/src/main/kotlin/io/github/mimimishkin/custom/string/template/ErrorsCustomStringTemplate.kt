package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.ANNOTATION_USE_SITE
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement

object ErrorsCustomStringTemplate : KtDiagnosticsContainer() {
    val NOT_A_TEMPLATE_PROCESSOR by warning0<KtElement>()

    val NOTHING_TO_INTERPOLATE by warning0<KtElement>()

    val NOT_A_CONSTANT by error0<KtElement>()

    val LOCAL_TEMPLATE_PROCESSOR by error0<KtElement>(ANNOTATION_USE_SITE)

    override fun getRendererFactory() = renderer
    private val renderer = object : BaseDiagnosticRendererFactory() {
        override val MAP by KtDiagnosticFactoryToRendererMap(CustomStringTemplate.ID) {
            it.put(NOT_A_TEMPLATE_PROCESSOR, "Did you forget to annotate your fun with @TemplateProcessor?")
            it.put(NOTHING_TO_INTERPOLATE, "@TemplateProcessor without any StringTemplate param.")
            it.put(NOT_A_CONSTANT, "Only constant values can be interpolated.")
            it.put(LOCAL_TEMPLATE_PROCESSOR, "Template processors can't be local yet.")
        }
    }
}