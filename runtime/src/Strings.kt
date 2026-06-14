package io.github.mimimishkin.custom.string.template

internal const val PLUGIN_ID = "io.github.mimimishkin.custom-string-template"

internal const val GITHUB_LINK = "https://github.com/mimimishkin/custom-string-template"

internal const val CALL_WITHOUT_PLUGIN = "Interpolation can only be done with a '$PLUGIN_ID' Kotlin compiler plugin " +
        "being enabled. Without it, calling to this method will throw an `AssertionError`." +
        "\n" +
        "Warning: opting this in will just suppress this error, but runtime error won't be solved."

internal const val ERROR_PLUGIN_DISABLED = "An attempt to use custom string template interpolator without a plugin. " +
        "To use this function a compiler plugin is required: '$PLUGIN_ID'. " +
        "See how to enable: $GITHUB_LINK"