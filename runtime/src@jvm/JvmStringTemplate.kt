package io.github.mimimishkin.custom.string.template

import io.github.mimimishkin.custom.string.template.StringTemplate as KotlinStringTemplate
import java.lang.StringTemplate as JavaStringTemplate

fun KotlinStringTemplate.Companion.JavaTemplateProcessor() =
    JavaStringTemplate.Processor<KotlinStringTemplate<Any?>, IllegalArgumentException> { template ->
        KotlinStringTemplate(template.fragments(), template.values())
    }