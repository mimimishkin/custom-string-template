@file:OptIn(ExperimentalCompilerApi::class)

package io.github.mimimishkin.custom.string.template

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.todo

class DiagnosticsTest : BaseTemplateTest() {

    @Test
    fun `string template param without annotation gives warning`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate

            fun FOO(string: StringTemplate): String = string.reconstruct()
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        assert(result.messages.contains("Did you forget to annotate your fun with @TemplateProcessor?")) {
            result.messages
        }
    }

    @Test
    fun `annotation without string template param gives warning`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.TemplateProcessor

            @TemplateProcessor
            fun FOO(): String = "nothing"
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        assert(result.messages.contains("@TemplateProcessor without any StringTemplate param.")) { result.messages }
    }

    @Test
    fun `local template processor gives error`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate
            import io.github.mimimishkin.custom.string.template.TemplateProcessor

            fun outer() {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }
        """)
        val result = compile(file)
        assert(result.messages.contains("Template processors can't be local yet.")) { result.messages }
    }

    @Test
    fun `string template receiver without annotation gives warning`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate

            fun StringTemplate.FOO(): String = this.reconstruct()
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        assert(result.messages.contains("Did you forget to annotate your fun with @TemplateProcessor?")) { result.messages }
    }

    @Test
    fun `annotation with multiple StringTemplate params compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(a: StringTemplate, b: StringTemplate): String =
                a.reconstruct() + b.reconstruct()

            fun check() {
                val result: String = FOO("hello", "42")
            }
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `annotation with StringTemplate context param compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            context(string: StringTemplate)
            fun FOO(tag: String): String =
                "<$tag>${string.reconstruct()}</$tag>"

            fun result(): String {
                return context("hello") { FOO("div") }
            }
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }

        todo {
            // in kotlin 2.4.10 explicit context arguments are not stabilized yet, so this test should fail
            val result = compileAndGetResult(file)
            assert(result == "<div>hello</div>")
        }
    }

    @Test
    fun `template processor without StringTemplate receiver or param but with annotation`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.TemplateProcessor

            @TemplateProcessor
            fun FOO(x: Int): Int = x * 2
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        assert(result.messages.contains("@TemplateProcessor without any StringTemplate param.")) { result.messages }
    }

    @Test
    fun `function with StringTemplate param and TemplateProcessor annotation compiles clean`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() {
                val result: String = FOO("test")
            }
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        assert(!result.messages.contains("Did you forget")) { result.messages }
        assert(!result.messages.contains("without any StringTemplate")) { result.messages }
    }

    @Test
    fun `companion object template processor compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate
            import io.github.mimimishkin.custom.string.template.TemplateProcessor

            class Processor {
                companion object {
                    @TemplateProcessor
                    fun FOO(string: StringTemplate): String = string.reconstruct()
                }
            }
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `object template processor compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate
            import io.github.mimimishkin.custom.string.template.TemplateProcessor

            object Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `override with template processor in subclass gives facade override error`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            interface Base {
                fun FOO(string: StringTemplate): String
            }

            class Impl : Base {
                @TemplateProcessor
                override fun FOO(string: StringTemplate): String = string.reconstruct()
            }
        """)
        val result = compile(file)
        assert(result.messages.contains("Cannot override template processor facade function.")) { result.messages }
    }

    @Test
    fun `override with template processor in open subclass gives facade override error`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Base {
                @TemplateProcessor
                open fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Derived : Base() {
                @TemplateProcessor
                override fun FOO(string: StringTemplate): String = string.reconstruct()
            }
        """)
        val result = compile(file)
        assert(result.messages.contains("Cannot override template processor facade function.")) { result.messages }
    }

    @Test
    fun `override without template processor in subclass gives warning not error`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Base {
                @TemplateProcessor
                open fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Derived : Base() {
                override fun FOO(string: StringTemplate): String = string.reconstruct()
            }
        """)
        val result = compile(file)
        assert(result.messages.contains("Did you forget to annotate your fun with @TemplateProcessor?")) { result.messages }
        assert(!result.messages.contains("Cannot override template processor facade function.")) { result.messages }
    }

    @Test
    fun `non-template override in subclass compiles fine`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate

            open class Base {
                open fun greet(): String = "hello"
            }

            class Derived : Base() {
                override fun greet(): String = "hi"
            }
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        assert(!result.messages.contains("Cannot override")) { result.messages }
    }

    @Test
    fun `subclass without override of template processor compiles`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Base {
                @TemplateProcessor
                open fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Derived : Base()
        """)
        val result = compile(file)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `override of template processor function through intermediate class gives facade override error`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Base {
                @TemplateProcessor
                open fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            open class Middle : Base()

            class Derived : Middle() {
                @TemplateProcessor
                override fun FOO(string: StringTemplate): String = string.reconstruct()
            }
        """)
        val result = compile(file)
        assert(result.messages.contains("Cannot override template processor facade function.")) { result.messages }
    }

    @Test
    fun `override with template processor in interface implementation gives facade override error`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            interface Processor {
                fun FOO(string: StringTemplate): String
            }

            abstract class AbstractImpl : Processor {
                @TemplateProcessor
                override fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class ConcreteImpl : AbstractImpl()
        """)
        val result = compile(file)
        assert(result.messages.contains("Cannot override template processor facade function.")) { result.messages }
    }
}
