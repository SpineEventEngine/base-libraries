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

package io.spine.format

import io.kotest.matchers.shouldBe
import java.io.File
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`File` and `Path` extensions should")
internal class FileExtsSpec {

    @Test
    fun `identify files with supported formats`() {
        val supportedFiles = listOf(
            "file.binpb", "file.pb", "file.bin",
            "file.pb.json", "file.json", "file.yml", "file.yaml"
        )
        supportedFiles.forEach { fileName ->
            File(fileName).hasSupportedFormat() shouldBe true
        }
    }

    @Test
    fun `reject files with unsupported formats`() {
        val unsupportedFiles = listOf(
            "file.txt", "file.jpeg", "file.gif", "file.exe"
        )
        unsupportedFiles.forEach { fileName ->
            File(fileName).hasSupportedFormat() shouldBe false
        }
    }

    @Nested
    inner class `'ensureFileExtension' function` {

        @Test
        fun `adds or replaces extension`() {
            val name = "example"
            val names = arrayOf(
                name,           // No extension.
                "$name.txt",    // Unsupported.
                "$name.json",   // Supported.
                "$name.yaml",   // Also supported.
                "$name.pb.json" // Special case of "complex" supported extension.
            )
            val files = names.map { File(it) }

            Format.entries.forEach { format ->
                files.forEach { file ->
                    format.matches(file.ensureFormatExtension(format))
                }
            }
        }

        @Test
        fun `returning the same instance if the extension is already present`() {
            val file = File("my.yaml")
            (file.ensureFormatExtension(Format.Yaml) === file) shouldBe true
        }
    }
}
