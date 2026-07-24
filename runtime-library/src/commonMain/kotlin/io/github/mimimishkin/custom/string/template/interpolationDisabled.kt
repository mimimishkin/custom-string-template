package io.github.mimimishkin.custom.string.template

/**
 * Throws [AssertionError] with an instruction of how to enable plugin.
 */
fun interpolationDisabled(): Nothing = throw AssertionError(ERROR_PLUGIN_DISABLED)