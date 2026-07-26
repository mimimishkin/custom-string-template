package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.ANNOTATION_USE_SITE
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement

object ErrorsCustomStringTemplate : KtDiagnosticsContainer() {
    private val renderer = object : BaseDiagnosticRendererFactory() {
        override val MAP by KtDiagnosticFactoryToRendererMap(CustomStringTemplate.ID) {
            it.put(NOT_A_TEMPLATE_PROCESSOR, "Did you forget to annotate your fun with @TemplateProcessor?")
            it.put(NOTHING_TO_INTERPOLATE, "@TemplateProcessor without any StringTemplate param.")
            it.put(NOT_A_LITERAL_OR_TEMPLATE, "Only string literals and string templates can be used as template processor arguments.")
            it.put(LOCAL_TEMPLATE_PROCESSOR, "Template processors can't be local yet.")
            it.put(NULLABLE_STRING_TEMPLATE, "StringTemplate parameter must not be nullable.")
            it.put(DEFAULT_PARAM_IN_TEMPLATE_PROCESSOR, "Template processors cannot have default parameter values.")
            it.put(FACADE_OVERRIDE, "Cannot override template processor facade function.")
            it.put(MUTABLE_TEMPLATE_PROCESSOR, "@TemplateProcessor is not allowed on var properties, use val instead.")
        }
    }

    override fun getRendererFactory() = renderer

    val NOT_A_TEMPLATE_PROCESSOR by warning0<KtElement>()

    val NOTHING_TO_INTERPOLATE by warning0<KtElement>()

    val NOT_A_LITERAL_OR_TEMPLATE by error0<KtElement>()

    val LOCAL_TEMPLATE_PROCESSOR by error0<KtElement>(ANNOTATION_USE_SITE)

    val NULLABLE_STRING_TEMPLATE by error0<KtElement>()

    val DEFAULT_PARAM_IN_TEMPLATE_PROCESSOR by error0<KtElement>()

    val FACADE_OVERRIDE by error0<KtElement>()

    val MUTABLE_TEMPLATE_PROCESSOR by error0<KtElement>()
}