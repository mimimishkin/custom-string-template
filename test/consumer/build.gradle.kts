plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.mimimishkin.custom-string-template") version "0.1.0"
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    //noinspection UseTomlInstead
    implementation("io.github.mimimishkin:producer:1.0.0")
}