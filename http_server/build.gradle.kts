plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    implementation(project(":common"))
    testImplementation(kotlin("test"))
    implementation("org.nanohttpd:nanohttpd:2.3.0")
}

val appClassName = "com.eyeo.ctu.diffsub2.ServerApp"

application {
    // Define the main class for the application.
    getMainClass().set(appClassName)
    mainClassName = appClassName
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("server-all.jar")
}