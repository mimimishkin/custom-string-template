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
 * Marks a function as a custom string template processor.
 *
 * The annotated function must accept at least one [StringTemplate] parameter — as a value parameter,
 * context parameter, or receiver. The `custom-string-template` compiler plugin generates a sibling
 * "facade" function where each [StringTemplate] parameter is replaced with a [String] parameter,
 * allowing callers to use ordinary string templates:
 *
 * ```kotlin
 * @TemplateProcessor
 * fun PROCESS(string: StringTemplate): String = string.reconstruct()
 *
 * // The plugin generates a facade:
 * // fun process(string: String): String
 *
 * val result = PROCESS("Hello, $name!") // works at call site
 * ```
 *
 * At IR level, calls to the generated facade are rewritten to invoke the original function with
 * [StringTemplate] whose [surroundings][StringTemplate.surroundings] and [holes][StringTemplate.holes]
 * are derived from the string literal or string template at the call site.
 *
 * ### Restrictions
 * - Must not be a local function.
 * - [StringTemplate] parameters must not be nullable.
 * - parameters can't have default values.
 * - Must not override another function.
 *
 * @see StringTemplate
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class TemplateProcessor