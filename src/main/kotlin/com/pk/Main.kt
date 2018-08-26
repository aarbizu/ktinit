package com.pk

import com.pk.Option.*

fun main(args: Array<String>) {
    val inputs = mutableMapOf<Option, Any>()
    for (option in listOf(GROUP_ID, ARTIFACT_ID)) {
        print("enter ${option.name}:")
        val input = readLine()!!
        inputs[option] = input
    }
    println("inputs = ${inputs}")

    // tack on other params
    inputs[MAIN_CLASS] = "${inputs[GROUP_ID]}.${inputs[ARTIFACT_ID]}.MainKt"
    inputs[DEPS] = dependencies()

    KtGradleProject(inputs, buildOverlaysForSimpleProject(inputs)).create()

}

// TODO Option to pass these in
fun dependencies(): List<Dependency> = listOf(
    Dependency("compile", "org.slf4j", "slf4j-api"),
    Dependency("compile", "org.slf4j", "slf4j-simple"),
    Dependency("compile", "com.squareup.okhttp3", "okhttp"),
    Dependency("compile", "com.google.code.gson", "gson"),
    Dependency("compile", "com.google.guava", "guava"),
    Dependency("testCompile", "io.kotlintest", "kotlintest"),
    Dependency("testCompile", "com.github.stefanbirkner", "system-rules"),
    Dependency("testCompile", "com.google.truth", "truth")
)

fun buildOverlaysForSimpleProject(inputs: MutableMap<Option, Any>): List<Overlay> {
    val ctx = inputs.mapKeys { it.key.templateName }
    val group = inputs[GROUP_ID]!!.toString().replace(".", "/")
    val pkg = "$group/${inputs[ARTIFACT_ID]}"

    return listOf(
        Overlay("build.gradle.mustache", "build.gradle", ctx),
        Overlay("gradle.properties.mustache", "gradle.properties", ctx),
        Overlay("Makefile.mustache", "Makefile", ctx),
        Overlay("README.md.mustache", "README.md", ctx),
        Overlay("gitignore.mustache", ".gitignore", ctx),
        Overlay("Main.mustache", "src/main/kotlin/$pkg/Main.kt", ctx),
        Overlay("SillyTest.kt.mustache", "src/test/kotlin/$pkg/SillyTest.kt", ctx)
    )
}

enum class Option(val templateName: String) {
    GROUP_ID("groupId"),
    ARTIFACT_ID("artifactId"),
    MAIN_CLASS("mainClass"),
    DEPS("deps")
}

