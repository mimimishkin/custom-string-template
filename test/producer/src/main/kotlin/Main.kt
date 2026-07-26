package producer

import io.github.mimimishkin.custom.string.template.StringTemplate
import io.github.mimimishkin.custom.string.template.TemplateProcessor

class Main {
    @TemplateProcessor
    fun FOO(string: StringTemplate): String = buildString {
        val surr = string.surroundings.iterator()
        val holes = string.holes.iterator()
        while (holes.hasNext()) {
            append(surr.next())
            append("'")
            append(holes.next())
            append("'")
        }
        append(surr.next())
    }
}
