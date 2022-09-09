plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.reincarnatey"
version = "1.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        dependencies {
            exclude(dependency("org.xerial:sqlite-jdbc:3.39.2.1"))
            exclude(dependency("org.ktorm:ktorm-core:3.5.0"))
            exclude(dependency("org.ktorm:ktorm-jackson:3.5.0"))
            exclude(dependency("com.fasterxml.jackson.core:jackson-core:2.13.4"))
            exclude(dependency("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4"))
        }
    }
}

dependencies{
    implementation("org.xerial:sqlite-jdbc:3.39.2.1")
    implementation("org.ktorm:ktorm-core:3.5.0")
    implementation("org.ktorm:ktorm-jackson:3.5.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
}