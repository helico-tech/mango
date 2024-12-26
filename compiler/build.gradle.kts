plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("lang.mango.CompilerKt")
    this.applicationName = "mangoc"
}

dependencies {
    implementation(projects.core)
}