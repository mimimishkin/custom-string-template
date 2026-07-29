plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.mimimishkin.custom-string-template") version "2.4.10-0.1.1"
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    //noinspection UseTomlInstead
    implementation("io.github.mimimishkin:producer:1.0.0")
}