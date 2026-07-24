@file:OptIn(ExperimentalCompilerApi::class)

package io.github.mimimishkin.custom.string.template

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

object CustomStringTemplate {
    const val ID = "io.github.mimimishkin.custom-string-template"

    val enabled = CompilerConfigurationKey<Boolean>("enabled")
}

@AutoService(CommandLineProcessor::class)
class CustomStringTemplateCliProcessor : CommandLineProcessor {
    override val pluginId = CustomStringTemplate.ID

    val enabledOption = CliOption(
        optionName = "enabled",
        valueDescription = "<true|false>",
        description = "If you want to enable custom-string-template plugin. Enabled by default.",
        required = false
    )
    override val pluginOptions = listOf(enabledOption)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option) {
            enabledOption -> configuration.put(CustomStringTemplate.enabled, value.toBoolean())
        }
    }
}

@AutoService(CompilerPluginRegistrar::class)
class CustomStringTemplateRegistrar : CompilerPluginRegistrar() {
    override val pluginId = CustomStringTemplate.ID

    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[CustomStringTemplate.enabled, true]) {
            FirExtensionRegistrar.registerExtension(FirRegistrar)
            IrGenerationExtension.registerExtension(IrExtension)
        }
    }

    object FirRegistrar : FirExtensionRegistrar() {
        override fun ExtensionRegistrarContext.configurePlugin() {
            +::RightStringTemplateUseChecker
            +::FirTemplateProcessorFacadeGenerator
        }
    }

    object IrExtension : IrGenerationExtension {
        override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
            moduleFragment.transform(IrTemplateProcessorFacadeUseActualizer(pluginContext), null)
        }
    }
}