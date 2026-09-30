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
import io.spine.testing.TestValues
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

@DisplayName("`Ensure` functions should")
internal class EnsureSpec {

    @Nested internal inner class
    `handle files via` {

        private lateinit var file: File

        @BeforeEach
        fun createFile(@TempDir tempDir: Path) {
            val testFolder = tempDir.toFile()
            val fileName = "ensure/exists/file" + TestValues.randomString() + ".txt"
            file = File(testFolder.absolutePath, fileName)
        }

        @Test
        fun `'File' argument`() {
            ensureFile(file)
            file.exists() shouldBe true
            file.isDirectory shouldBe false
        }

        @Test
        fun `returning an existing 'File' as-is`() {
            ensureFile(file)
            file.exists() shouldBe true

            // Now that the file exists, `ensureFile` returns it without changes.
            ensureFile(file) shouldBe file
            file.exists() shouldBe true
        }

        @Test
        fun `'Path' argument`() {
            val path = file.toPath()
            val returnedValue: Any = ensureFile(path)

            file.exists() shouldBe true
            returnedValue shouldBe path
        }

        @Test
        fun `rejecting a directory passed as 'File'`(@TempDir tempDir: Path) {
            assertThrows<IllegalArgumentException> {
                ensureFile(tempDir.toFile())
            }
        }
    }

    @Nested internal inner class
    `handle a directory creation` {

        private lateinit var tempDir: Path

        @BeforeEach
        fun createTempDir(@TempDir tempDir: Path) {
            this.tempDir = tempDir
        }

        @Test
        fun `if it does not exist`() {
            val subDir = Paths.get(
                "sub-1-" + TestValues.randomString(),
                "sub-2-" + TestValues.randomString()
            )
            val newDir = tempDir.resolve(subDir)

            // See that the directory does not exist.
            newDir.toFile().exists() shouldBe false

            ensureDirectory(newDir)

            newDir.isDirectory() shouldBe true
        }

        @Test
        fun `if it exists`() {
            val existingDir = tempDir.resolve(TestValues.randomString())
            ensureDirectory(existingDir)

            // Now as we know that the directory exists, let's try it again.
            ensureDirectory(existingDir)
            existingDir.isDirectory() shouldBe true
        }

        @Test
        fun `rejecting existing file`() {
            val filePath = tempDir.resolve("file" + TestValues.randomString())
            val file = filePath.toFile()
            ensureFile(file)
            assertThrows<IllegalStateException> {
                ensureDirectory(filePath)
            }
        }
    }
}
