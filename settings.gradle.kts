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