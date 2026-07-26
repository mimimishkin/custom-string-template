@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenLocal()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
}

include("gradle-plugin")
include("kotlin-compiler-plugin")
include("runtime-library")