package io.github.mimimishkin.custom.string.template

import kotlin.jvm.JvmRecord
import kotlin.jvm.JvmStatic

/**
 * TODO:
 */
@JvmRecord
data class StringTemplate<out T>(val surroundings: List<String>, val holes: List<T>) {
    init {
        require(surroundings.size == holes.size + 1) {
            "Size of surroundings must be bigger than holes by exactly 1. " +
                    "Current sizes: surroundings=${surroundings.size}, holes=${holes.size}"
        }
    }

//    fun reconstruct(): String = buildString {
//        for (i in holes.indices) {
//            append(surroundings[i])
//            append(holes[i])
//        }
//        append(surroundings.last())
//    }

    companion object {
        internal const val HOLES_DIVIDER = "\u001F"

        /**
         * Allow to create an interpolation parameter manually, when the custom string template plugin cannot be used
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
        fun <T> of(buildTemplate: WithoutPluginBuilder<T>.() -> String): StringTemplate<T> {
            val builder = WithoutPluginBuilder<T>()
            val string = builder.buildTemplate()
            val surroundings = string.split(HOLES_DIVIDER)
            return StringTemplate(surroundings, builder.holes)
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
        fun <T> wholeOf(string: String): StringTemplate<T> {
            return StringTemplate(listOf(string), emptyList())
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
        fun <T> wholeOf(any: T): StringTemplate<T> {
            return StringTemplate(listOf("", ""), listOf(any))
        }
    }

    class WithoutPluginBuilder<T> {
        internal val holes = mutableListOf<T>()

        fun hole(hole: T): String {
            holes += hole
            return HOLES_DIVIDER
        }

        operator fun T.unaryPlus() = hole(this)
    }
}