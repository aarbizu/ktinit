import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

object Properties {
    const val kotlin_version = "1.9.10"
    const val mustache_version = "0.9.11"
    const val okhttp_version = "4.9.3"
    const val gson_version = "2.10.1"
    const val guava_version = "32.1.3-jre"
    const val system_rules_version = "1.19.0"
    const val truth_version = "1.1.5"
    const val jupiter_version = "5.10.1"
    const val kotlinx_cli_version = "0.3.6"
    const val moshi_version = "1.15.0"
    const val slf4j_version = "2.0.9"
    const val json_path_version = "2.8.0"
    const val zt_exec_version = "1.12"
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.10"     // I would like to reference a const here but that doesn't work from the command line even tho Intellij is OK with it

    // Apply the application plugin to add support for building a CLI application.
    application

    // Apply the idea plugin
    idea

    // spotless
    id("com.diffplug.spotless") version "6.23.2"

    // this plugin helps us publish to maven repositories (like GitHub packages)
    `maven-publish`

    jacoco

    id("com.github.ben-manes.versions") version "0.50.0"
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Properties.kotlin_version}")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Properties.kotlin_version}")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Properties.jupiter_version}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Properties.jupiter_version}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Properties.jupiter_version}")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:${Properties.kotlinx_cli_version}")
    implementation("com.squareup.moshi:moshi:${Properties.moshi_version}")
    implementation("com.squareup.moshi:moshi-kotlin:${Properties.moshi_version}")
    implementation("org.slf4j:slf4j-api:${Properties.slf4j_version}")
    implementation("org.slf4j:slf4j-simple:${Properties.slf4j_version}")
    implementation("com.jayway.jsonpath:json-path:${Properties.json_path_version}")
    implementation("org.zeroturnaround:zt-exec:${Properties.zt_exec_version}")
    implementation("com.github.spullara.mustache.java:compiler:${Properties.mustache_version}")
    implementation("com.squareup.okhttp3:okhttp:${Properties.okhttp_version}")
    implementation("com.google.code.gson:gson:${Properties.gson_version}")
    implementation("com.google.guava:guava:${Properties.guava_version}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Properties.kotlin_version}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Properties.kotlin_version}")
    testImplementation("com.github.stefanbirkner:system-rules:${Properties.system_rules_version}")
    testImplementation("com.google.truth:truth:${Properties.truth_version}")
}

group = "com.pk"
version = "0.0.2"

kotlin {
    jvmToolchain(17)
}

application {
    // Define the main class for the application.
    mainClass.set("com.pk.MainKt")
}

spotless {
    kotlin {
        ktlint()
    }
}

tasks {

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, FAILED, SKIPPED)
    }
}

// https://help.github.com/en/github/managing-packages-with-github-packages/configuring-gradle-for-use-with-github-packages
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/pallavkothari/ktinit")

            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
        // easier to debug with ./gradlew publishKtinitPublicationToInternalRepository
        maven {
            name = "internal"
            url = uri("${layout.buildDirectory}/repos/internal")
        }
    }
    publications {
        register("ktinit", MavenPublication::class) {
            from(components["java"])
        }
    }
}

// build sources jar too
java {
    withSourcesJar()
}
