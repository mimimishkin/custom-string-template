package io.github.mimimishkin.custom.string.template

/**
 * Throws [AssertionError] with an instruction of how to enable plugin.
 */
@PublishedApi
internal fun <T> interpolationDisabled(): T = throw AssertionError(ERROR_PLUGIN_DISABLED)