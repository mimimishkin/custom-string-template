package io.github.mimimishkin.custom.string.template

/**
 * Marks that interpolator requires a `io.github.mimimishkin.custom-string-template` Kotlin compiler plugin to
 * be used.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@RequiresOptIn(CALL_WITHOUT_PLUGIN)
annotation class FacadeInterpolatorCall

/**
 * TODO:
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class TemplateProcessor

// TODO: add highlighting to templates if possible