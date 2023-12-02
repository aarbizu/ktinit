package com.pk

import com.google.common.io.Files
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class FilesTests {
    @Test
    fun testLs() {
        val dir = "/tmp"
        val traverser = Files.fileTraverser()
        traverser.breadthFirst(File(dir)).forEach { file ->
            println(file.absolutePath)
        }
    }

    @Test
    fun copyResourceToFile() {
        val tmp = createTempDirectory().toFile()
        copyResourceToDir("gradlew", tmp)
        val gradlew = File(tmp, "gradlew")
        assertTrue(gradlew.exists())
        assertTrue(gradlew.isFile)
    }
}
