@file:Suppress("UnstableApiUsage")

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.mimimishkin"
version = libs.versions.customStringTemplate.get()

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlin.gradlePluginApi)
}

gradlePlugin {
    website = "https://github.com/mimimishkin/custom-string-template"
    vcsUrl = "https://github.com/mimimishkin/custom-string-template"

    // Define the plugin
    val customStringTemplate by plugins.creating {
        id = "io.github.mimimishkin.custom-string-template"
        implementationClass = "io.github.mimimishkin.custom.string.template.CustomStringTemplatePlugin"
        displayName = "Custom StringTemplate"
        description = "Allows to create a string template processors like modern Java does."
        tags = listOf("string", "template", "custom")
    }
}

val compilerPlugin = libs.customStringTemplate.compilerPlugin.get()
val runtime = libs.customStringTemplate.runtime.get()
buildConfig {
    packageName("io.github.mimimishkin.custom.string.template")

    buildConfigField<String>("PLUGIN_ID", "io.github.mimimishkin.custom.string.template")
    buildConfigField<String>("PLUGIN_GROUP", compilerPlugin.group)
    buildConfigField<String>("PLUGIN_ARTIFACT_ID", compilerPlugin.name)
    buildConfigField<String?>("PLUGIN_VERSION", compilerPlugin.version)
    buildConfigField<String>("RUNTIME_LIBRARY", runtime.toString())
}