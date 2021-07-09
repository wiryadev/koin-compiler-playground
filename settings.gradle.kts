pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "playground"

//include(":workload")
include(":koin-annotations")
include(":koin-compiler")
include(":coffee-maker")
include(":coffee-maker2")