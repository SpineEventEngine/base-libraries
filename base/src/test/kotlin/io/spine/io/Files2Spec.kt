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
import io.spine.io.Files2.existsNonEmpty
import io.spine.testing.TestValues
import io.spine.testing.UtilityClassTest
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file.Path
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`Files2` utility class should")
internal class Files2Spec : UtilityClassTest<Files2>(Files2::class.java) {

    private var testFolder: File? = null

    @BeforeEach
    fun setUp(@TempDir testFolderPath: Path) {
        testFolder = testFolderPath.toFile()
    }

    @Nested internal inner class
    `verify that an existing file is not empty` {

        @Test
        fun `returning 'false' when existing file is empty`() {
            val emptyFile = testFolder!!.toPath().resolve("empty file").toFile()

            existsNonEmpty(emptyFile) shouldBe false
        }

        @Test
        fun `returning 'false' when a file does not exist`() {
            val doesNotExist = File(TestValues.randomString())

            existsNonEmpty(doesNotExist) shouldBe false
        }

        @Test
        fun `returning 'true' if the existing file is not empty`() {
            val nonEmptyFile = testFolder!!.toPath().resolve("non-empty file").toFile()
            val path = nonEmptyFile.absolutePath
            val charsetName = Charset.defaultCharset().name()
            PrintWriter(path, charsetName).use { out -> out.println(TestValues.randomString()) }

            existsNonEmpty(nonEmptyFile) shouldBe true
        }
    }

    @Test
    fun `obtain absolute path`() {
        val file = Files2.toAbsolute("some/dir/file.txt")
        file.isAbsolute shouldBe true
    }
}
