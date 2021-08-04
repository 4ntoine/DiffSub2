plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    implementation(project(":common"))
    testImplementation(kotlin("test"))
    implementation("io.reflectoring.diffparser:diffparser:1.4")
    implementation("org.slf4j:slf4j-nop:1.7.32") // to hide slf4j "Not configured" output
    implementation("org.apache.kafka:kafka-clients:2.8.0")
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