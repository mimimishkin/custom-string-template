[![Gradle Plugin](https://img.shields.io/maven-metadata/v?metadataUrl=https://plugins.gradle.org/m2/io/github/mimimishkin/custom-string-template/io.github.mimimishkin.custom-string-template.gradle.plugin/maven-metadata.xml&logo=gradle&logoColor=white&label=Gradle%20Plugin&color=02303A)](https://plugins.gradle.org/plugin/io.github.mimimishkin.custom-string-template)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-2.4.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-A31F34.svg)](https://opensource.org/licenses/MIT)

# Custom StringTemplate

A Kotlin compiler plugin that lets you define custom string interpolation by writing a single function.

## Motivation

Kotlin's built-in string templates (`"$variable"`) are powerful but there's no way to customize what happens to each
interpolated value. Common use cases that are hard to express today:

- **SQL injection prevention** – turn `"SELECT * FROM users WHERE id = $userId"` into a prepared statement call
  automatically.
- **HTML escaping** – escape every interpolated value before inserting it into markup.

With this plugin you write a function that describes *how* to process the template, and the compiler takes care of
splitting the string literal into parts and calling your function at compile time.

## How it works

1. **You write** a function (or extension property) that receives a `StringTemplate`. The `StringTemplate` interface
   exposes two members:
   - `surroundings: List<String>` – the constant parts of the literal, one more than the number of holes.
   - `holes: List<Any?>` – the interpolated expressions.

2. **You annotate** that function (or property) with `@TemplateProcessor`.

3. **The plugin generates** a sibling function with the same name that accepts regular `String` parameters instead of
   `StringTemplate` parameters. Inside that generated function the plugin constructs a `StringTemplate` from the string
   literal and forwards it to your original function.

Because the generated function is the one that compiler actually resolves at call sites, the call looks like an ordinary
string literal:

```kotlin
val result: String = FOO("Hello, $name!")
```

### Example

A processor that wraps every interpolated value in double quotes:

```kotlin
@TemplateProcessor
fun StringBuilder.appendQuoted(string: StringTemplate): StringBuilder {
    val partsIterator = string.surroundings.iterator()
    append(partsIterator.next())

    for (param in string.holes) {
        append('"').append(param).append('"')
        append(partsIterator.next())
    }
    return this
}
```

The plugin generates:

```kotlin
fun StringBuilder.appendQuoted(string: String): StringBuilder
```

Call site:

```kotlin
val builder = StringBuilder()
builder.appendQuoted("Hello, $name!")
// Surroundings: ["Hello, ", "!"]
// Holes: [name]
// Result: builder.appendQuoted with each hole wrapped in quotes
```

### Extension properties

Template processors also work as extension properties with a `StringTemplate` receiver. The getter receives the
template and returns the processed result:

```kotlin
@TemplateProcessor
val StringTemplate.quoted: String
    get() = buildString {
        val partsIterator = surroundings.iterator()
        append(partsIterator.next())
        for (param in holes) {
            append('"').append(param).append('"')
            append(partsIterator.next())
        }
    }
```

Usage:

```kotlin
val result: String = "Hello, $name!".quoted
```

### Context parameters

Context parameters are supported as well, but it must be a string or string template literal, which is only possible 
with explicit context arguments.

## Naming convention

Template processor functions and properties should use **SCREAMING_SNAKE_CASE** (e.g. `FOO`, `HTML_DIV`, `SQL_QUERY`).

```kotlin
val result: String = FOO("Hello, $name!")
```

SCREAMING_SNAKE_CASE makes it easy to distinguish template processors from regular functions at a glance inside
implementation files where both kinds coexist.

## Usage

To use it in your project, add this to `build.gradle.kts`:
```kotlin
plugins {
    id("io.github.mimimishkin.custom-string-template") version "2.4.10-0.1.2"
}
```

Note that while this is enough to compile and work properly, you will get a false error in the IDE -
`Argument type mismatch: actual type is 'String', but 'StringTemplate' was expected`. This is due to the fact that 
IntelliJ IDEA runs only bundled compiler plugins (e.g. 'serialization', 'all-open') for code analysis. To enable
external plugins, you need to install 
[KEFS](https://plugins.jetbrains.com/plugin/26480-kotlin-external-fir-support) and add an artifact
`io.github.mimimishkin:custom-string-template-compiler-plugin` located in Maven Central. 

[Here](https://github.com/Mr3zee/Kotlin-External-FIR-Support/blob/main/GUIDE.md) you can find more info about working 
with third-party compiler plugins. I will maintain compatibility only with compiler version that the latest stable
IntelliJ IDEA uses.

## Restrictions

- **No nullable `StringTemplate` parameters** – every `StringTemplate` parameter (value, context, or receiver) must be
  non-nullable.
- **No default parameter values** – parameters with defaults would make it horrible for the plugin to fight with 
  synthetic `$default` function.
- **`val` only** – `@TemplateProcessor` cannot be applied to `var` properties.
- **No local declarations** – template processors must be declared at class or file level.
- **Kotlin only** – the plugin operates at the IR level, so generated facades are not available from Java, JavaScript,
  or other targets. The generated facades are hided from other platforms.

## StringTemplate decomposition

The `surroundings` and `holes` lists always follow this pattern:

```
surrounding₀  hole₀  surrounding₁  hole₁  ...  holeₙ  surroundingₙ₊₁
```

There is always one more surrounding than hole. Both the leading and trailing surrounding may be empty strings.

**Example:** `"$number + 2 = ${number + 2}"`

| Part         | Value                  |
|--------------|------------------------|
| surroundings | `["", " + 2 = ", ""]`  |
| holes        | `[number, number + 2]` |

## Generated function annotations

The generated facade function carries the `@FacadeInterpolatorCall` annotation, which is an `@OptIn`-level marker.
This prevents calling the facade directly from code that doesn't opt in, which would bypass the plugin and produce
incorrect results at runtime. Normal call sites that use string literals are resolved by the compiler and do not
trigger this restriction.
