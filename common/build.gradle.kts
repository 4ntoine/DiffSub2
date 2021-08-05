plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    api("com.beust:jcommander:1.78")
    implementation("io.reflectoring.diffparser:diffparser:1.4")
}