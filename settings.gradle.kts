@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenLocal()
        // for IDEA releses
        maven { url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
    }
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
        // for IDEA releses
        maven { url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("runtime-library")
include("kotlin-compiler-plugin")
include("gradle-plugin")

val pluginDir = file("${System.getProperty("user.home")}/.m2/repository/io/github/mimimishkin/custom-string-template")
if (pluginDir.exists()) {
    include("test:producer")
    include(":test:consumer")
}