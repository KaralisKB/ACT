// Top-level build file where you can add configuration options common to all sub-projects/modules.
//android {
//    compileSdk = 31
//
//    defaultConfig {
//        applicationId = "com.example.act"
//        minSdk = 21
//        targetSdk = 31
//    }
//}
//
//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        classpath("com.android.tools.build:gradle:8.0.0")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10") // Add Kotlin Gradle plugin version
//    }
//}
//
//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//    }
//}
//
//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}








































plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}























