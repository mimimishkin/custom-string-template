# Custom StringTemplate Processor

A small Kotlin plugin that lets you write your own string interpolation logic without the usual ceremony.

## What's this for?

Ever wanted to write something like `log.debug("User ${getName()} has score ${getScore()}")` and have it only evaluate the
interpolated expressions when debug logging is actually enabled? Or `html.div("Hello $name")` that automatically escapes
`name` for you? Or `sql.query("SELECT * FROM users WHERE id = $userId")` that safely turns `$userId` into a prepared
statement parameter?

This plugin lets you define a function that processes a `StringTemplate` - and it generates the rest for you.

## How it works

You write a function that accepts a `StringTemplate` (with `surroundings` and `holes` properties) and annotate it with
`@TemplateProcessor`.  
The plugin generates a sibling function that accepts a normal `String` and does the interpolation at IR level.

### Example

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
@FacadeInterpolatorCall
fun StringBuilder.appendQuoted(string: String)
```

Now you can call: `builder.appendQuoted("Hello, $name!")` and each param value gets wrapped in quotes
automatically.

## Important notes

- `surroundings` and `holes` always follow the pattern:  
  `<surrounding><hole><surrounding>...<hole><surrounding>`  
  First and last elements are always `surroundings` (even if empty).
- Example: `"$number + 2 = ${number + 2}"`  
  → surroundings: `["", " + 2 = ", ""]`  
  → holes: `[number, number + 2]`
- The generated function is marked `@RequiresOptIn` (`@FacadeInterpolatorCall`) to prevent accidental usage without the
  plugin.
- This works at IR level, so it's **Kotlin-only** – export to other languages is disabled.