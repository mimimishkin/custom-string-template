package io.github.mimimishkin.custom.string.template

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class CustomStringTemplatePlugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        kotlinCompilation.defaultSourceSet.dependencies {
            implementation(BuildConfig.RUNTIME_LIBRARY)
        }

        kotlinCompilation.compileTaskProvider.configure {
            it.compilerOptions.optIn.add("io.github.mimimishkin.custom.string.template.FacadeInterpolatorCall")
        }

        return kotlinCompilation.project.provider { emptyList() }
    }

    override fun getCompilerPluginId(): String {
        return BuildConfig.PLUGIN_ID
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(BuildConfig.PLUGIN_GROUP, BuildConfig.PLUGIN_ARTIFACT_ID, BuildConfig.PLUGIN_VERSION)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }
}