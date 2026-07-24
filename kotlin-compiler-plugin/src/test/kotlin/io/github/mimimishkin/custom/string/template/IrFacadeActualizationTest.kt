@file:OptIn(ExperimentalCompilerApi::class)

package io.github.mimimishkin.custom.string.template

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.todo

class IrFacadeActualizationTest : BaseTemplateTest() {

    @Test
    fun `facade function accepts string argument`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("hello")
        """)
        assert(compileAndGetResult(file) == "hello")
    }

    @Test
    fun `facade replaces string template receiver with string`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun StringTemplate.FOO(): String = this.reconstruct()

            fun result(): String = "hello".FOO()
        """)
        assert(compileAndGetResult(file) == "hello")
    }

    @Test
    fun `facade preserves non-string-template params`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate, count: Int): String = string.reconstruct().repeat(count)

            fun result(): String = FOO("ha", 3)
        """)
        assert(compileAndGetResult(file) == "hahaha")
    }

    @Test
    fun `string constant argument compiles through ir actualizer`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("Hello")
        """)
        assert(compileAndGetResult(file) == "Hello")
    }

    @Test
    fun `string concatenation argument compiles through ir actualizer`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val param = 42
                return FOO("Hello, $param")
            }
        """)
        assert(compileAndGetResult(file) == "Hello, 42")
    }

    @Test
    fun `multiple string concatenation arguments compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val a = "A"
                val b = "B"
                return FOO("$a and $b")
            }
        """)
        assert(compileAndGetResult(file) == "A and B")
    }

    @Test
    fun `nested string template expressions compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = 1
                return FOO("${ "${x + 1}" }")
            }
        """)
        assert(compileAndGetResult(file) == "2")
    }

    @Test
    fun `empty string constant compiles through ir actualizer`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("")
        """)
        assert(compileAndGetResult(file) == "")
    }

    @Test
    fun `ordinal concatenation of two string literals does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("Hello" + "World")
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `string template concatenation with string literal does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val name = "world"
                return FOO("Hello, $name!" + "hii")
            }
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `string literal with template concatenation does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val name = "world"
                return FOO("Hello, " + "$name!")
            }
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `multiple string template params work`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun combine(a: StringTemplate, b: StringTemplate): String =
                a.reconstruct() + b.reconstruct()

            fun result(): String = combine("Hello", "World")
        """)
        assert(compileAndGetResult(file) == "HelloWorld")
    }

    @Test
    fun `mixed string and non-string template params work`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun process(template: StringTemplate, count: Int): String =
                template.reconstruct().repeat(count)

            fun result(): String = process("Ha", 3)
        """)
        assert(compileAndGetResult(file) == "HaHaHa")
    }

    @Test
    fun `template processor with receiver and value params`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun StringTemplate.wrap(prefix: String): String =
                prefix + this.reconstruct()

            fun result(): String = "hello".wrap("[")
        """)
        assert(compileAndGetResult(file) == "[hello")
    }

    @Test
    fun `template processor called as extension function`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun StringTemplate.shout(): String =
                this.reconstruct().uppercase()

            fun result(): String = "hello".shout()
        """)
        assert(compileAndGetResult(file) == "HELLO")
    }

    @Test
    fun `multiple processors with different implementations`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun reconstruct(string: StringTemplate): String = string.reconstruct()

            @TemplateProcessor
            fun shout(string: StringTemplate): String = string.reconstruct().uppercase()

            fun result(): String {
                val a: String = reconstruct("hello")
                val b: String = shout("world")
                return a + b
            }
        """)
        assert(compileAndGetResult(file) == "helloWORLD")
    }

    @Test
    fun `template processor accessing class properties works`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor(private val prefix: String) {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = prefix + string.reconstruct()
            }

            fun result(): String = Processor("[").FOO("hello")
        """)
        assert(compileAndGetResult(file) == "[hello")
    }

    @Test
    fun `multiple template processors in same class compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()

                @TemplateProcessor
                fun BAR(string: StringTemplate): String = string.reconstruct().uppercase()
            }

            fun result(): String {
                val p = Processor()
                return p.FOO("hello") + p.BAR("world")
            }
        """)
        assert(compileAndGetResult(file) == "helloWORLD")
    }

    @Test
    fun `string template with multiple interpolation holes compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = 1
                val y = 2
                return FOO("$x + $y = ${x + y}")
            }
        """)
        assert(compileAndGetResult(file) == "1 + 2 = 3")
    }

    @Test
    fun `empty string argument compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("")
        """)
        assert(compileAndGetResult(file) == "")
    }

    @Test
    fun `unicode string compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("Hello \u00e4\u00f6\u00fc")
        """)
        assert(compileAndGetResult(file) == "Hello äöü")
    }

    @Test
    fun `escaped characters in string template compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("line1\nline2\ttab")
        """)
        assert(compileAndGetResult(file) == "line1\nline2\ttab")
    }

    @Test
    fun `string template with trailing expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = 10
                return FOO("value: $x")
            }
        """)
        assert(compileAndGetResult(file) == "value: 10")
    }

    @Test
    fun `string template with expression at start compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = 10
                return FOO("$x is the value")
            }
        """)
        assert(compileAndGetResult(file) == "10 is the value")
    }

    @Test
    fun `string template with expression in middle compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = 10
                return FOO("start $x end")
            }
        """)
        assert(compileAndGetResult(file) == "start 10 end")
    }

    @Test
    fun `string template with adjacent expressions compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = 1
                val y = 2
                return FOO("$x$y")
            }
        """)
        assert(compileAndGetResult(file) == "12")
    }

    @Test
    fun `string template with lambda expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("${ listOf(1, 2, 3).size }")
        """)
        assert(compileAndGetResult(file) == "3")
    }

    @Test
    fun `string template with null expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x: String? = null
                return FOO("$x")
            }
        """)
        assert(compileAndGetResult(file) == "null")
    }

    @Test
    fun `string template with boolean expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val b = true
                return FOO("$b")
            }
        """)
        assert(compileAndGetResult(file) == "true")
    }

    @Test
    fun `string template with char expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val c = 'A'
                return FOO("$c")
            }
        """)
        assert(compileAndGetResult(file) == "A")
    }

    @Test
    fun `string template with long expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val l = 123456789L
                return FOO("$l")
            }
        """)
        assert(compileAndGetResult(file) == "123456789")
    }

    @Test
    fun `string template with double expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val d = 3.14
                return FOO("$d")
            }
        """)
        assert(compileAndGetResult(file) == "3.14")
    }

    @Test
    fun `string template with float expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val f = 2.5f
                return FOO("$f")
            }
        """)
        assert(compileAndGetResult(file) == "2.5")
    }

    @Test
    fun `string template with byte expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val b: Byte = 127
                return FOO("$b")
            }
        """)
        assert(compileAndGetResult(file) == "127")
    }

    @Test
    fun `string template with short expression compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val s: Short = 1000
                return FOO("$s")
            }
        """)
        assert(compileAndGetResult(file) == "1000")
    }

    @Test
    fun `string variable argument does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val s: String = "hello"
                return FOO(s)
            }
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `function call result argument does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            fun getString(): String = "hello"

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO(getString())
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `conditional expression argument does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = true
                return FOO(if (x) "a" else "b")
            }
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `triple literal concatenation does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("a" + "b" + "c")
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `double literal concatenation with spaces does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String = FOO("a" + " " + "b")
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `string plus variable concatenation does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = "world"
                return FOO("hello" + x)
            }
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `variable plus string concatenation does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val x = "hello"
                return FOO(x + "world")
            }
        """)
        assertFailsToCompile(file)
    }

    @Test
    fun `two variables concatenation does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val a = "hello"
                val b = "world"
                return FOO(a + b)
            }
        """)
        assertFailsToCompile(file)
    }
}
