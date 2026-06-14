import io.github.mimimishkin.custom.string.template.StringTemplate
import io.github.mimimishkin.custom.string.template.TemplateProcessor
import kotlin.test.Test

@TemplateProcessor
fun StringBuilder.appendQuoted(string: StringTemplate<*>): StringBuilder {
    for (i in string.holes.indices) {
        append(string.surroundings[i])
        append('"').append(string.holes[i]).append('"')
    }
    append(string.surroundings.last())
    return this
}

class GeneralTest {
    @Test
    fun `string template fun is created`() {
        buildString {
            val name = "Bob"
            appendQuoted("Hello, $name!")
        }
    }
}