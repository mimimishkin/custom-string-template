@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.mavenPublish)
}

val compilerPlugin = libs.customStringTemplate.compilerPlugin.get()
group = compilerPlugin.group
version = compilerPlugin.version!!
description = "Allows to create a string template processors like modern Java does."

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
    plugins.register("custom-string-template") {
        id = "io.github.mimimishkin.custom-string-template"
        implementationClass = "io.github.mimimishkin.custom.string.template.CustomStringTemplatePlugin"
        displayName = "Custom StringTemplate"
        description = project.description
        tags = listOf("string", "template", "custom")
    }
}

val runtime = libs.customStringTemplate.runtime.get()
buildConfig {
    packageName("io.github.mimimishkin.custom.string.template")

    buildConfigField<String>("PLUGIN_ID", "io.github.mimimishkin.custom.string.template")
    buildConfigField<String>("PLUGIN_GROUP", compilerPlugin.group)
    buildConfigField<String>("PLUGIN_ARTIFACT_ID", compilerPlugin.name)
    buildConfigField<String>("PLUGIN_VERSION", compilerPlugin.version!!)
    buildConfigField<String>("RUNTIME_LIBRARY", runtime.toString())
}