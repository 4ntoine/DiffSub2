plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    implementation(project(":common"))
    testImplementation(kotlin("test"))
    implementation("org.apache.kafka:kafka-clients:2.8.0")
}

val appClassName = "com.eyeo.ctu.diffsub2.ClientApp"

application {
    // Define the main class for the application.
    getMainClass().set(appClassName)
    mainClassName = appClassName
}