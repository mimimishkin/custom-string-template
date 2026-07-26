import producer.FOO

fun main() {
    val name = "Custom StringTemplate"
    assert(FOO("Hello, $name") == "Hello, '$name'")
}