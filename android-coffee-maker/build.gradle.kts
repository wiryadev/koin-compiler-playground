val koinVersion: String by project
val appcompatVersion : String by project

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    idea
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "org.gradle.kotlin.dsl.samples.androidstudio"
        minSdkVersion(21)
//        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation(project(":koin-annotations"))
    ksp(project(":koin-compiler"))
    implementation(project(":coffee-maker2"))

    implementation ("androidx.appcompat:appcompat:$appcompatVersion")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}