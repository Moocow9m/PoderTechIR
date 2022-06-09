plugins {
    kotlin("jvm") version "1.7.0"
    id("org.graalvm.buildtools.native")
}

group = "tech.poder.ir"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation(kotlin("reflect"))
    implementation(platform(kotlin("bom")))
    testImplementation("org.apiguardian:apiguardian-api:1.1.2") //Fix for JUnit > 1.7.X
    testRuntimeOnly("org.junit.platform:junit-platform-commons:1.8.2")
    testImplementation(kotlin("test"))
}

tasks {

    /*val javaDestination = compileJava.get().destinationDirectory.get()
    val javaTestDestination = compileTestJava.get().destinationDirectory.get()
    compileKotlin {
        destinationDirectory.set(javaDestination)
    }
    compileTestKotlin {
        destinationDirectory.set(javaTestDestination)
    }*/

    //val javaVersionCompat = JavaVersion.VERSION_16.toString()
    val javaVersion = JavaVersion.VERSION_17.toString()

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        //dependsOn(clean)
        //sourceCompatibility = javaVersion
        //targetCompatibility = javaVersion
        kotlinOptions.jvmTarget = javaVersion
        //kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        //kotlinOptions.languageVersion = "1.6"
        //kotlinOptions.apiVersion = "1.6"
        //kotlinOptions.useFir = true
    }

    withType<JavaCompile> {
        //dependsOn(clean)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs("--add-modules=jdk.incubator.foreign", "--enable-native-access=ALL-UNNAMED")
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