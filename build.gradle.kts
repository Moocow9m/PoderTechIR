plugins {
    kotlin("jvm") version "1.5.30-RC"
}

group = "tech.poder.ir"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {

    withType<JavaCompile> {
        sourceCompatibility = "16"
        targetCompatibility = "16"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.apiVersion = "1.6"
        kotlinOptions.languageVersion = "1.6"
        kotlinOptions.useFir = true
        sourceCompatibility = "16"
        targetCompatibility = "16"
    }
}