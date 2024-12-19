plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.evandhardspace"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
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