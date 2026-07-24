package io.github.mimimishkin.custom.string.template

import kotlin.jvm.JvmRecord
import kotlin.jvm.JvmStatic

/**
 * TODO:
 */
interface StringTemplate {
    /**
     * List of string fragments that surround each [hole][holes] value. Size of this list is always larger that [holes]
     * by 1. Some fragments can be empty: in the start and the end of the string and between two holes if there are no
     * space between them.
     */
    val surroundings: List<String>

    /**
     * List of object values that is surrounded by strings.
     */
    val holes: List<Any?>

    fun reconstruct(): String = buildString {
        for (i in holes.indices) {
            append(surroundings[i])
            append(holes[i])
        }
        append(surroundings.last())
    }

    companion object {
        internal const val HOLES_DIVIDER = "\u001F"

        /**
         * Allow to create an interpolation parameter manually, when the custom-string-template plugin cannot be used
         * (including Java code).
         *
         * For example:
         * ```
         * val x: Int
         * val y: Int
         * of {
         *     hole(x) + " + " + hole(y) + " = " + hole(x + y)
         * }
         * ```
         * Will produce the same result as if `"$x + $y = ${x + y}"` when plugin is used.
         */
        @JvmStatic
        fun <T> of(buildTemplate: WithoutPluginBuilder<T>.() -> String): StringTemplate {
            val builder = WithoutPluginBuilder<T>()
            val string = builder.buildTemplate()

            val surroundings = string.split(HOLES_DIVIDER)
            val holes = builder.holes
            require(surroundings.size == holes.size + 1) {
                "Size of surroundings must be bigger than holes by exactly 1. " +
                        "Current sizes: surroundings=${surroundings.size}, holes=${holes.size}"
            }

            return SimpleStringTemplate(surroundings, holes)
        }

        /**
         * Wraps [string] into an interpolation parameter as if it was just whole string literal.
         *
         * For example:
         * ```
         * wholeOf("meow")
         * ```
         * Will produce the same result as if `"meow"` when plugin is used.
         */
        @JvmStatic
        fun <T> wholeOf(string: String): StringTemplate {
            return SimpleStringTemplate(listOf(string), emptyList())
        }

        /**
         * Wraps [any] into an interpolation parameter as if it was enclosed by string quotes.
         *
         * For example:
         * ```
         * any: Any?
         * wholeOf(any)
         * ```
         * Will produce the same result as if `"$any"` when plugin is used.
         */
        @JvmStatic
        fun wholeOf(any: Any?): StringTemplate {
            return SimpleStringTemplate(listOf("", ""), listOf(any))
        }
    }

    /**
     * Allow to create a [StringTemplate] without a `custom-string-template` plugin via small dsl.
     */
    class WithoutPluginBuilder<T> {
        internal val holes = mutableListOf<T>()

        fun hole(hole: T): String {
            holes += hole
            return HOLES_DIVIDER
        }

        operator fun T.unaryPlus() = hole(this)
    }
}

/**
 * Simple [StringTemplate] implementation with eagle fragments and values initialization and without a size
 * check.
 */
@PublishedApi
internal data class SimpleStringTemplate(
    override val surroundings: List<String>,
    override val holes: List<Any?>
) : StringTemplate