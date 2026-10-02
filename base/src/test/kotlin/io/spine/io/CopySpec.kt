/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.io

import io.kotest.matchers.shouldBe
import io.spine.io.Copy.copyContent
import io.spine.io.Copy.copyDir
import io.spine.testing.UtilityClassTest
import java.nio.file.Files.createDirectory
import java.nio.file.Files.exists
import java.nio.file.Files.write
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`Copy` utilities should")
internal class CopySpec : UtilityClassTest<Copy>(Copy::class.java) {

    companion object {
        const val sourceDirectory = "to-copy"
        const val destinationDirectory = "dest"
        const val subDirectory = "sub-dir"
        const val subDirLevel2 = "nested-dir"
        const val file1 = "f1.bytes"
        const val file2 = "f2.bytes"
        private val fileSize = mapOf(
            file1 to 100,
            file2 to 200
        )
        fun sizeOf(fileName: String) = fileSize[fileName] ?: 42 // something non-zero
    }

    private lateinit var srcDir: Path
    private lateinit var destDir: Path

    @BeforeEach
    fun createDirectories(@TempDir tempDir: Path) {

        /** Creates a file with the sequence of bytes of the specified size. */
        fun createFile(dir: Path, fileName: String, size: Int) {
            val path = dir.resolve(fileName)
            val content = ByteArray(size)
            for (i in 0 until size) {
                content[i] = i.toByte()
            }
            write(path, content, CREATE)
        }

        /** Creates a file with the size associated with its name. */
        fun createFile(dir: Path, fileName: String) {
            createFile(dir, fileName, sizeOf(fileName))
        }

        srcDir = tempDir.resolve(sourceDirectory)
        createDirectory(srcDir)
        createFile(srcDir, file1)
        createFile(srcDir, file2)

        val subDir = srcDir.resolve(subDirectory)
        createDirectory(subDir)
        createFile(subDir, file1)
        createFile(subDir, file2)

        val deeperDir = subDir.resolve(subDirLevel2)
        createDirectory(deeperDir)
        createFile(deeperDir, file1)
        createFile(deeperDir, file2)

        destDir = tempDir.resolve(destinationDirectory)
        createDirectory(destDir)
    }

    @Test
    fun `copy a directory recursively`() {
        copyDir(srcDir, destDir)

        val resultDir = destDir.resolve(sourceDirectory)
        assertExists(resultDir)
        assertFile(resultDir, file1)
        assertFile(resultDir, file2)

        val subDir = resultDir.resolve(subDirectory)
        assertExists(subDir)
        assertFile(subDir, file1)
        assertFile(subDir, file2)

        val deeperDir = subDir.resolve(subDirLevel2)
        assertExists(subDir)
        assertFile(deeperDir, file1)
        assertFile(deeperDir, file2)
    }

    @Test
    fun `copy a directory with filtering`() {
        copyDir(srcDir, destDir) { path ->
            val name = path.toString()
            name.contains("f2") || name.endsWith("sub-dir")
        }

        val resultDir = destDir.resolve(sourceDirectory)
        assertExists(resultDir)
        assertFile(resultDir, file2)
        assertDoesNotExist(resultDir.resolve(file1))

        val subDir = resultDir.resolve(subDirectory)
        assertExists(subDir)
        assertFile(subDir, file2)
        assertDoesNotExist(subDir.resolve(file1))

        val deeperDir = subDir.resolve(subDirLevel2)
        assertExists(deeperDir)
        assertFile(deeperDir, file2)
        assertDoesNotExist(deeperDir.resolve(file1))
    }

    @Test
    fun `copy directory content`() {
        copyContent(srcDir, destDir)

        assertDoesNotExist(destDir.resolve(sourceDirectory))
        assertFile(destDir, file1)
        assertFile(destDir, file2)

        val subDir = destDir.resolve(subDirectory)
        assertExists(subDir)
        assertFile(subDir, file1)
        assertFile(subDir, file2)

        val deeperDir = subDir.resolve(subDirLevel2)
        assertExists(subDir)
        assertFile(deeperDir, file1)
        assertFile(deeperDir, file2)
    }

    @Test
    fun `copy directory content with filtering`() {
        copyContent(srcDir, destDir) { path ->
            val name = path.toString()
            name.contains("f2") || name.endsWith("sub-dir")
        }

        assertDoesNotExist(destDir.resolve(sourceDirectory))
        assertFile(destDir, file2)
        assertDoesNotExist(destDir.resolve(file1))

        val subDir = destDir.resolve(subDirectory)
        assertExists(subDir)
        assertFile(subDir, file2)
        assertDoesNotExist(subDir.resolve(file1))

        val deeperDir = subDir.resolve(subDirLevel2)
        assertExists(deeperDir)
        assertFile(deeperDir, file2)
        assertDoesNotExist(deeperDir.resolve(file1))
    }

    private fun assertExists(path: Path) {
        assertTrue(exists(path), "Expected to exist: `${path}`.")
    }

    private fun assertDoesNotExist(path: Path) {
        assertFalse(exists(path), "Expected to NOT exist: `${path}`.")
    }

    private fun assertFile(dir: Path, fileName: String) {
        val path = dir.resolve(fileName)
        val expectedSize = sizeOf(fileName)

        assertExists(path)
        val file = path.toFile()
        file.isFile shouldBe true

        file.length() shouldBe expectedSize
    }
}
