import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.eyeo.ctu"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.reflectoring.diffparser:diffparser:1.4")
    implementation("org.slf4j:slf4j-nop:1.7.32") // to hide slf4j "Not configured" output
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

val appClassName = "com.eyeo.ctu.diffsub2.ServerApp"

application {
    // Define the main class for the application.
    getMainClass().set(appClassName)
    mainClassName = appClassName
}