import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin(libs.versions.jvm.get()) version libs.versions.kotlin.get()

    // Apply the application plugin to add support for building a CLI application.
    application

    // Apply the idea plugin
    idea

    // spotless
    alias(libs.plugins.spotless)

    // this plugin helps us publish to maven repositories (like GitHub packages)
    `maven-publish`

    jacoco

    alias(libs.plugins.versions)
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)

    // Use the Kotlin JUnit integration.
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.bundles.junit.jupiter)

    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.cli)
    implementation(libs.bundles.moshi)
    implementation(libs.bundles.slf4j)
    implementation(libs.jsonpath)
    implementation(libs.zeroturnaround)
    implementation(libs.mustache)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.guava)

    testImplementation(libs.system.rules)
    testImplementation(libs.truth)
    testRuntimeOnly(libs.junit.platform.launcher)
}

group = "com.pk"
version = "0.0.2"

kotlin {
    jvmToolchain(21)
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    compileTestKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }
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
