package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.name.StandardClassIds

internal object Symbols {
    val dependencyPackage = "io.github.mimimishkin.custom.string.template".fqn()

    val FacadeInterpolatorCall = dependencyPackage.topClassId("FacadeInterpolatorCall")

    val TemplateProcessor = dependencyPackage.topClassId("TemplateProcessor")

    val interpolationDisabled = dependencyPackage.topCallableId("interpolationDisabled")

    val StringTemplate = dependencyPackage.topClassId("StringTemplate")

    val SimpleStringTemplate = dependencyPackage.topClassId("SimpleStringTemplate")

    val listOf = StandardClassIds.BASE_COLLECTIONS_PACKAGE.topCallableId("listOf")
}