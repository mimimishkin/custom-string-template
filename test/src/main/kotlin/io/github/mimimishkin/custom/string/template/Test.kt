package io.github.mimimishkin.custom.string.template

@TemplateProcessor
fun meow(string: StringTemplate<*>): String {
    return buildString {
        val surr = string.surroundings.iterator()
        append(surr.next())
        for (hole in string.holes) {
            append("meow ")
            append(hole)
            append("meow ")
            append(surr.next())
        }
    }
}

fun main() {
    meow("Hello, ${"world"}!")
}