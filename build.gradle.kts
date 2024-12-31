plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.evandhardspace"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1") // Check for latest version
    testImplementation("com.google.jimfs:jimfs:1.3.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xcontext-receivers"))
    }
}