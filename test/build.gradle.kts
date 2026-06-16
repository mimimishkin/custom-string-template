plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.mimimishkin.custom-string-template") version "0.0.1"
}

kotlin {
    jvmToolchain(17)
}