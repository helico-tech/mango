allprojects {
    group = "nl.helico.mango"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
}