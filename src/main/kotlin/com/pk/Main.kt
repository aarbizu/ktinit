package com.pk

import com.google.common.base.CaseFormat
import com.pk.Option.ARTIFACT_ID
import com.pk.Option.DEPS
import com.pk.Option.GROUP_ID
import com.pk.Option.MAIN_CLASS
import com.pk.Option.NO_ARG_PARSING
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import java.io.File
import kotlin.io.path.createTempDirectory

fun main(args: Array<String>) {
    val parser = ArgParser("ktinit")
    val currentDir by parser
        .option(ArgType.Boolean, shortName = "c", fullName = "current-dir", description = "create project in current directory")
        .default(false)

    val groupId by parser
        .option(ArgType.String, shortName = "g", fullName = "group-id", description = "the group ID for this project")
        .default("com.example")

    val artifactId by parser
        .option(ArgType.String, shortName = "a", fullName = "artifact-id", description = "the artifact ID for this project")
        .default("ktfoo")

    val dependencies by parser
        .option(
            ArgType.String,
            shortName = "d",
            fullName = "dep",
            description = "provide additional dependencies in the format <groupId>:<artifactId>[:version]",
        ).multiple()

    val noArgs by parser
        .option(
            ArgType.Boolean,
            shortName = "n",
            fullName = "no-arg-parsing",
            description = "don't add command line arg parsing capabilities",
        ).default(false)

    parser.parse(args)

    if (currentDir) println("Creating project in current directory.")

    val artifactIdNormalized = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, artifactId)

    val inputs =
        mutableMapOf<Option, Any>(
            GROUP_ID to groupId,
            ARTIFACT_ID to artifactIdNormalized,
            NO_ARG_PARSING to noArgs,
        )

    val defaultDependencies = dependencies().toMutableList()

    if (noArgs) {
        println("Disabling command-line parsing.")
        defaultDependencies.removeIf { dep -> dep.artifact == "kotlinx-cli" }
    }

    val projectParams =
        ProjectParams(
            groupId = groupId,
            artifactId = artifactId,
            overlays = buildOverlaysForSimpleProject(inputs, deps = parseDependencies(dependencies).union(defaultDependencies)),
            location = if (currentDir) File(System.getProperty("user.dir")) else createTempDirectory().toFile(),
        )

    KtGradleProject(projectParams).create()

    if (!currentDir) println("\nYou may use the project above or run `ktinit --help` to see more options.")
}

fun parseDependencies(deps: List<String>): List<Dependency> =
    deps.map {
        val parts = it.split(":")
        when (parts.size) {
            2 -> Dependency(group = parts[0], artifact = parts[1])
            3 -> Dependency(group = parts[0], artifact = parts[1], pinnedVersion = parts[2])
            else -> throw Exception(it)
        }
    }

data class ProjectParams(
    val groupId: String,
    val artifactId: String,
    val location: File = createTempDirectory().toFile(),
    val overlays: List<Overlay>,
)

fun dependencies(): List<Dependency> =
    listOf(
        Dependency("implementation", "org.slf4j", "slf4j-api"),
        Dependency("implementation", "org.slf4j", "slf4j-simple"),
        Dependency("implementation", "com.squareup.okhttp3", "okhttp"),
        Dependency("implementation", "com.google.code.gson", "gson"),
        Dependency("implementation", "com.google.guava", "guava"),
        Dependency("testImplementation", "com.github.stefanbirkner", "system-rules"),
        Dependency("testImplementation", "com.google.truth", "truth"),
        Dependency("testRuntimeOnly", "org.junit.jupiter", "junit-jupiter-engine"),
        Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-api"),
        Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-params"),
        Dependency("testRuntimeOnly", "org.junit.platform", "junit-platform-launcher"),
        Dependency("implementation", "org.jetbrains.kotlinx", "kotlinx-cli"),
    )

fun buildOverlaysForSimpleProject(
    inputs: MutableMap<Option, Any>,
    deps: Iterable<Dependency> = dependencies(),
): List<Overlay> {
    // build ctx to pass to Mustache
    inputs[MAIN_CLASS] = "${inputs[GROUP_ID]}.${inputs[ARTIFACT_ID]}.MainKt"
    inputs[DEPS] = deps
    val ctx = inputs.mapKeys { it.key.templateName }
    val group = inputs[GROUP_ID]!!.toString().replace(".", "/")
    val pkg = "$group/${inputs[ARTIFACT_ID]}"

    return listOf(
        Overlay("build.gradle.kts.mustache", "build.gradle.kts", ctx),
        Overlay("gradle.properties.mustache", "gradle.properties", ctx),
        Overlay("Makefile.mustache", "Makefile", ctx),
        Overlay("README.md.mustache", "README.md", ctx),
        Overlay("gitignore.mustache", ".gitignore", ctx),
        Overlay("Main.mustache", "src/main/kotlin/$pkg/Main.kt", ctx),
        Overlay("SillyTest.kt.mustache", "src/test/kotlin/$pkg/SillyTest.kt", ctx),
        Overlay("libs.versions.toml.mustache", "gradle/libs.versions.toml", ctx),
    )
}

// keeping these around for Mustache context keys
enum class Option(
    val templateName: String,
) {
    GROUP_ID("groupId"),
    ARTIFACT_ID("artifactId"),
    MAIN_CLASS("mainClass"),
    DEPS("deps"),
    NO_ARG_PARSING("noArgs"),
}
