plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("lang.mango.RuntimeKt")
    this.applicationName = "mango"
}

dependencies {
    implementation(projects.core)
}