@file:OptIn(ExperimentalCompilerApi::class)

package io.github.mimimishkin.custom.string.template

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

abstract class BaseTemplateTest {
    protected fun compile(source: SourceFile): JvmCompilationResult = KotlinCompilation().run {
        sources = listOf(source)
        compilerPluginRegistrars += CustomStringTemplateRegistrar()
        commandLineProcessors += CustomStringTemplateCliProcessor()
        inheritClassPath = true
        messageOutputStream = System.out
        compile()
    }

    protected fun compileWithDisabledPlugin(source: SourceFile): JvmCompilationResult = KotlinCompilation().run {
        sources = listOf(source)
        compilerPluginRegistrars += CustomStringTemplateRegistrar()
        commandLineProcessors += CustomStringTemplateCliProcessor()
        pluginOptions = listOf(
            com.tschuchort.compiletesting.PluginOption(
                pluginId = CustomStringTemplate.ID,
                optionName = "enabled",
                optionValue = "false"
            )
        )
        inheritClassPath = true
        messageOutputStream = System.out
        compile()
    }

    protected fun compileAndGetResult(source: SourceFile): String {
        val result = compile(source)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
        val classLoader = result.classLoader
        try {
            return classLoader.loadClass("test.TestKt").getMethod("result").invoke(null) as String
        } catch (e: Exception) {
            throw AssertionError("Failed to invoke result()", e)
        }
    }

    protected fun assertCompiles(source: SourceFile) {
        val result = compile(source)
        assert(result.exitCode == KotlinCompilation.ExitCode.OK) { result.messages }
    }

    protected fun assertFailsToCompile(source: SourceFile) {
        val result = compile(source)
        assert(result.exitCode != KotlinCompilation.ExitCode.OK) { result.messages }
    }
}
