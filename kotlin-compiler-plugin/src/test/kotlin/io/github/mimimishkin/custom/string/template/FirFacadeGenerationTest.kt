@file:OptIn(ExperimentalCompilerApi::class)

package io.github.mimimishkin.custom.string.template

import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test

class FirFacadeGenerationTest : BaseTemplateTest() {

    @Test
    fun `top-level processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() { val result: String = FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `class-level processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `companion object processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                companion object {
                    @TemplateProcessor
                    fun FOO(string: StringTemplate): String = string.reconstruct()
                }
            }

            fun check() { val result: String = Processor.FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `object processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            object Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor.FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `multiple processors generate multiple facades`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            @TemplateProcessor
            fun BAR(string: StringTemplate): String = string.reconstruct().uppercase()

            fun check() { val a: String = FOO("x"); val b: String = BAR("y") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `multiple StringTemplate params generate facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(a: StringTemplate, b: StringTemplate): String =
                a.reconstruct() + b.reconstruct()

            fun check() { val result: String = FOO("a", "b") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `mixed params generate facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate, count: Int): String = string.reconstruct().repeat(count)

            fun check() { val result: String = FOO("ha", 3) }
        """)
        assertCompiles(file)
    }

    @Test
    fun `receiver param generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun StringTemplate.FOO(): String = this.reconstruct()

            fun check() { val result: String = "test".FOO() }
        """)
        assertCompiles(file)
    }

    @Test
    fun `receiver and value params generate facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun StringTemplate.wrap(prefix: String): String = prefix + this.reconstruct()

            fun check() { val result: String = "test".wrap("[") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `Int return type generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun countHoles(string: StringTemplate): Int = string.holes.size

            fun check() { val result: Int = countHoles("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `Unit return type generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate) { /* no return value */ }

            fun check() { FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `vararg param generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(vararg strings: StringTemplate): String = strings.joinToString("") { it.reconstruct() }

            fun check() { val result: String = FOO("a", "b") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `abstract class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            abstract class Base {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Concrete : Base()

            fun check() { val result: String = Concrete().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `open class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `open modifier generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Processor {
                @TemplateProcessor
                open fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `sealed class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            sealed class Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Concrete : Processor()

            fun check() { val result: String = Concrete().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `interface processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            interface Processor {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Impl : Processor {
                override fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Impl().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `data class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            data class Processor(val value: String) {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor("x").FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `nested class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Outer {
                class Inner {
                    @TemplateProcessor
                    fun FOO(string: StringTemplate): String = string.reconstruct()
                }
            }

            fun check() { val result: String = Outer.Inner().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `inner class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Outer {
                inner class Inner {
                    @TemplateProcessor
                    fun FOO(string: StringTemplate): String = string.reconstruct()
                }
            }

            fun check() { val result: String = Outer().Inner().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `suspend modifier generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            suspend fun FOO(string: StringTemplate): String = string.reconstruct()

            suspend fun check() { val result: String = FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `inline modifier generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            inline fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() { val result: String = FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `tailrec modifier generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            tailrec fun FOO(string: StringTemplate, n: Int): String =
                if (n <= 0) string.reconstruct() else FOO(string, n - 1)

            fun check() { val result: String = FOO("test", 1) }
        """)
        assertCompiles(file)
    }

    @Test
    fun `private visibility generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                @TemplateProcessor
                private fun FOO(string: StringTemplate): String = string.reconstruct()

                fun check() { val result: String = FOO("test") }
            }
        """)
        assertCompiles(file)
    }

    @Test
    fun `internal visibility generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            internal fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() { val result: String = FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `nested package generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test.inner.nested

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() { val result: String = FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `class with inheritance generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Base {
                open fun greet(): String = "hello"
            }

            class Processor : Base() {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `class with suspend modifier generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                @TemplateProcessor
                suspend fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            suspend fun check() { val result: String = Processor().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `class with companion object generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                companion object {
                    @TemplateProcessor
                    fun FOO(string: StringTemplate): String = string.reconstruct()
                }
            }

            fun check() { val result: String = Processor.FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `class alongside properties generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor {
                val name: String = "test"
                var count: Int = 0

                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            fun check() { val result: String = Processor().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `top-level alongside properties generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            val globalValue: String = "global"

            @TemplateProcessor
            fun FOO(string: StringTemplate): String = string.reconstruct()

            fun check() { val result: String = FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `multiple processors in same class generate facades`() {
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

            fun check() {
                val p = Processor()
                val a: String = p.FOO("x")
                val b: String = p.BAR("y")
            }
        """)
        assertCompiles(file)
    }

    @Test
    fun `generic top-level processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            @TemplateProcessor
            fun <T> FOO(string: StringTemplate, value: T): String = string.reconstruct() + value.toString()

            fun check() { val result: String = FOO("test", 42) }
        """)
        assertCompiles(file)
    }

    @Test
    fun `generic class processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            class Processor<T>(val value: T) {
                @TemplateProcessor
                fun FOO(string: StringTemplate): String = string.reconstruct() + value.toString()
            }

            fun check() { val result: String = Processor(42).FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `generic interface with template processor generates facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            interface Processor<T> {
                @TemplateProcessor
                fun FOO(string: StringTemplate, value: T): String
            }

            class Impl : Processor<String> {
                override fun FOO(string: StringTemplate, value: String): String = string.reconstruct() + value
            }

            fun check() { val result: String = Impl().FOO("test", "!") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `interface with annotated override generates facade at subclass level`() {
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

            fun check() { val result: String = Impl().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `override with annotation in superclass uses existing facade`() {
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
                override fun FOO(string: StringTemplate): String = string.reconstruct().uppercase()
            }

            fun check() { val result: String = Derived().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `override without annotation uses superclass facade`() {
        val file = SourceFile.kotlin("Test.kt", $$"""
            @file:OptIn(FacadeInterpolatorCall::class)

            package test

            import io.github.mimimishkin.custom.string.template.*

            open class Base {
                @TemplateProcessor
                open fun FOO(string: StringTemplate): String = string.reconstruct()
            }

            class Derived : Base() {
                override fun FOO(string: StringTemplate): String = string.reconstruct().uppercase()
            }

            fun check() { val result: String = Derived().FOO("test") }
        """)
        assertCompiles(file)
    }

    @Test
    fun `plugin disabled does not generate facade`() {
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
        assertFailsToCompile(file)
    }
}
