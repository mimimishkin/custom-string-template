import producer.Main

fun main() {
    val name = "Custom StringTemplate"
    assert(Main().FOO("Hello, $name") == "Hello, '$name'")
}