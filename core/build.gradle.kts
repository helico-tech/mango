plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {

    dependencies {
        implementation(libs.betterParse)
        testImplementation(kotlin("test"))
    }
}