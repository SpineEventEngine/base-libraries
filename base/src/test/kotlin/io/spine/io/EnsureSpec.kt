/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.spine.io

import io.kotest.matchers.shouldBe
import io.spine.io.Ensure.ensureDirectory
import io.spine.io.Ensure.ensureFile
import io.spine.testing.TestValues
import io.spine.testing.UtilityClassTest
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

@DisplayName("`Ensure` utilities class should")
internal class EnsureSpec : UtilityClassTest<Ensure>(Ensure::class.java) {

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
        fun `'Path' argument`() {
            val path = file.toPath()
            val returnedValue: Any = ensureFile(path)

            file.exists() shouldBe true
            returnedValue shouldBe path
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
