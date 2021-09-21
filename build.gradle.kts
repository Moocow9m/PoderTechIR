plugins {
    kotlin("jvm") version "1.5.31"
}

group = "tech.poder.ir"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("org.junit.platform:junit-platform-commons:1.7.0")
    testImplementation(kotlin("test"))
}

tasks {

    val javaDestination = compileJava.get().destinationDirectory.get()
    val javaTestDestination = compileTestJava.get().destinationDirectory.get()
    compileKotlin {
        destinationDirectory.set(javaDestination)
    }
    compileTestKotlin {
        destinationDirectory.set(javaTestDestination)
    }

    val javaVersionCompat = JavaVersion.VERSION_16.toString()
    val javaVersion = JavaVersion.VERSION_17.toString()

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        kotlinOptions.jvmTarget = javaVersionCompat
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        //kotlinOptions.languageVersion = "1.6"
        //kotlinOptions.apiVersion = "1.6"
        //kotlinOptions.useFir = true
    }

    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs("--add-modules=jdk.incubator.foreign", "--enable-native-access=PoderTechIR.main")
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    jar {
        dependsOn(sourcesJar)
    }

    artifacts {
        archives(sourcesJar)
        archives(jar)
    }
}