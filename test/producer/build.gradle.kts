plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.mimimishkin.custom-string-template") version "2.4.10-0.2.0"
    alias(libs.plugins.mavenPublish)
}

group = "io.github.mimimishkin"
version = "1.0.0"

kotlin {
    jvmToolchain(25)
}

mavenPublishing {
    coordinates(groupId = group.toString(), artifactId = name, version = version.toString())
}