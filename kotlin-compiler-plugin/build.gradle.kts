plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(17)
}

group = "io.github.mimimishkin"
description = "Allows to create a string template processors like modern Java does."
version = "0.0.1"

dependencies {
    compileOnly(libs.kotlin.compiler)
    implementation(libs.google.autoService)
    ksp(libs.zacsweers.autoServiceKsp)
}

mavenPublishing {
    val compilerPlugin = libs.customStringTemplate.compilerPlugin.get()
    coordinates(artifactId = compilerPlugin.name)
}