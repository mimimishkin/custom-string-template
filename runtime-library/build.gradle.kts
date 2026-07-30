@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmpLibrary)
//    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.mavenPublish)
}

val runtimeLibrary = libs.customStringTemplate.runtime.get()
group = runtimeLibrary.group
version = runtimeLibrary.version!!
description = "Annotation and a class required to write a custom string template processor."

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

    listOf(js(), wasmJs()).forEach {
        it.nodejs()
        it.browser()
    }
    wasmWasi {
        nodejs()
    }

    jvm()

    linuxArm64()
    linuxX64()

    macosArm64()

    mingwX64()

    tvosArm64()
    tvosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
}

mavenPublishing {
    coordinates(groupId = group.toString(), artifactId = runtimeLibrary.name, version = version.toString())

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    pom {
        name = "Custom StringTemplate runtime"
        description = project.description
        inceptionYear = "2026"
        url = "https://github.com/mimimishkin/custom-string-template"
        licenses {
            license {
                name = "MIT"
            }
        }
        developers {
            developer {
                id = "mimimishkin"
                name = "Mimimishkin"
                email = "printf.mika@gmail.com"
            }
        }
        scm {
            url = "https://github.com/mimimishkin/custom-string-template"
            connection = "scm:git:git://github.com/mimimishkin/custom-string-template"
        }
    }
}
