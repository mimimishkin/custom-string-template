plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(17)
}

val compilerPlugin = libs.customStringTemplate.compilerPlugin.get()
group = compilerPlugin.group
version = compilerPlugin.version!!
description = "Allows to create a string template processors like modern Java does."

dependencies {
    compileOnly(libs.kotlin.compiler)
    implementation(libs.google.autoService)
    ksp(libs.zacsweers.autoServiceKsp)

    testImplementation(kotlin("test"))
    testImplementation(project(":runtime-library"))
    testImplementation(libs.zacsweers.kctFork)
}

mavenPublishing {
    coordinates(groupId = group.toString(), artifactId = compilerPlugin.name, version = version.toString())

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    pom {
        name = "Custom StringTemplate compiler plugin"
        description = project.description
        inceptionYear = "2026"
        url = "https://github.com/mimimishkin/custom-string-template"
        licenses {
            license {
                name = "MIT"
            }
        }
        developers {
            developer {
                id = "mimimishkin"
                name = "Mimimishkin"
                email = "printf.mika@gmail.com"
            }
        }
        scm {
            url = "https://github.com/mimimishkin/custom-string-template"
            connection = "scm:git:git://github.com/mimimishkin/custom-string-template"
        }
    }
}