@file:OptIn(ExperimentalCompilerApi::class)

package io.github.mimimishkin.custom.string.template

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test

class CompilationFailureTest : BaseTemplateTest() {

    @Test
    fun `nullable string template doesn't compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*
            import kotlin.random.Random

            @TemplateProcessor
            fun FOO(string: StringTemplate?): String = string?.reconstruct() ?: ""
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
        assert(result.messages.contains("StringTemplate parameter must not be nullable.")) { result.messages }
    }

    @Test
    fun `plugin disabled via option does not generate facades`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            package test

            import io.github.mimimishkin.custom.string.template.StringTemplate
            import io.github.mimimishkin.custom.string.template.TemplateProcessor

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() {
                val result: String = FOO("Hello")
            }
        """)
        val result = compileWithDisabledPlugin(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `complex expression argument not compiles through ir actualizer`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*
            import kotlin.random.Random

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() {
                val param = Random.nextInt()
                val result: String = FOO(param.toString())
            }
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `string expression argument not compiles through ir actualizer`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() {
                val param = 42
                val result: String = FOO("Hello " + param + "!")
            }
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `ordinal concatenation with string variable does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val name = "world"
                return FOO("Hello, " + name + "!")
            }
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `ordinal concatenation with function call result does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun greet(): String = "Hello"

            fun result(): String = FOO(greet() + " World")
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
    }

    @Test
    fun `ordinal concatenation with integer variable does not compile`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun result(): String {
                val count = 42
                return FOO("Count: " + count)
            }
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
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
    }

    @Test
    fun `template processor with default parameter value`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate, prefix: String = "["): String =
                prefix + string.reconstruct()

            fun check() {
                val result: String = FOO("hello")
            }
        """)
        val result = compile(file)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
        assert(result.messages.contains("Template processors cannot have default parameter values.")) { result.messages }
    }
}
