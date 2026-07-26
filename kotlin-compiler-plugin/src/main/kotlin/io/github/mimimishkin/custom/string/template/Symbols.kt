package io.github.mimimishkin.custom.string.template

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

internal object Symbols {
    val dependencyPackage = "io.github.mimimishkin.custom.string.template".fqn()

    val FacadeInterpolatorCall = dependencyPackage.topClassId("FacadeInterpolatorCall")

    val TemplateProcessor = dependencyPackage.topClassId("TemplateProcessor")

    val interpolationDisabled = dependencyPackage.topCallableId("interpolationDisabled")

    val StringTemplate = dependencyPackage.topClassId("StringTemplate")

    val SimpleStringTemplate = dependencyPackage.topClassId("SimpleStringTemplate")

    val listOf = StandardClassIds.BASE_COLLECTIONS_PACKAGE.topCallableId("listOf")

    val ObjCName = ClassId.topLevel(FqName("kotlin.native.ObjCName"))
    val CName = ClassId.topLevel(FqName("kotlinx.cinterop.CName"))
    val JsExport = ClassId.topLevel(FqName("kotlin.js.JsExport"))

    val JvmSynthetic = ClassId.topLevel(FqName("kotlin.jvm.JvmSynthetic"))
    val HideFromObjC = ClassId.topLevel(FqName("kotlin.native.HideFromObjC"))
    val JsExportIgnore = JsExport.createNestedClassId(Name.identifier("Ignore"))
}