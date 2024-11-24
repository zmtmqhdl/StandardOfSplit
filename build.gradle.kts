// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.hilt.android.gradle.plugin.v250)
        classpath(libs.com.google.devtools.ksp.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    alias(libs.plugins.compose.compiler) apply false
}