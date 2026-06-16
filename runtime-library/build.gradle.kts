@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmpLibrary)
//    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.mimimishkin"
description = "Annotation and a class required to write a string template processor."
version = "0.0.1"

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.mimimishkin"
        compileSdk = 37
    }
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    js().nodejs()

    jvm()

    linuxArm64()
    linuxX64()

    macosArm64()

    mingwX64()

    tvosArm64()
    tvosSimulatorArm64()

    wasmJs().nodejs()
    wasmWasi().nodejs()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
}

mavenPublishing {
    val runtimeLibrary = libs.customStringTemplate.runtime.get()
    coordinates(artifactId = runtimeLibrary.name)
}
