package io.github.mimimishkin.custom.string.template

/**
 * Marks that a function is a generated facade for a @TemplateProcessor function.
 * Facades with this annotation automatically opt-in to allow seamless usage.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@DslMarker
@RequiresOptIn(CALL_WITHOUT_PLUGIN)
annotation class FacadeInterpolatorCall

/**
 * TODO:
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class TemplateProcessor