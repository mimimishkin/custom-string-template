plugins {
    // To use the same plugins in multiple modules, repeat them here with `apply false`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kmpLibrary) apply false
    alias(libs.plugins.kotlin.binaryCompatibilityValidator) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.mavenPublish) apply false
}