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
import io.kotest.matchers.shouldNotBe
import io.spine.format.FormatTest.Companion.tempDir
import io.spine.io.replaceExtension
import java.io.File
import java.util.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * The abstract base for format tests that create a [file] under
 * an automatically created [temporary directory][tempDir].
 *
 * @property format The format to test.
 * @property instance The instance of the type [T] to be used when writing files
 *   and comparing with the parsed values.
 * @property file The file path that is composed using the temporary directory
 *  as the parent, with a randomly generated UUID-based name.
 *  The file name has the extension supported by the given [format].
 *
 * @see setup
 */
abstract class FormatTest<T : Any>(
    protected val format: Format<in T>
) {
    protected lateinit var file: File
    protected lateinit var instance: T

    protected abstract fun createInstance(): T

    companion object {

        lateinit var tempDir: File

        @BeforeAll
        @JvmStatic
        fun createDirectory(@TempDir tempDir: File) {
            this.tempDir = tempDir
        }
    }

    @BeforeEach
    fun setup() {
        val name = UUID.randomUUID().toString()
        file = File(tempDir, name).ensureFormatExtension(format)
        instance = createInstance()
    }

    @Test
    fun `write an value to a file`() {
        write(file, format, instance)
        file.run {
            exists() shouldBe true
            length() shouldNotBe 0
        }
    }

    @Test
    fun `parse a file`() {
        write(file, format, instance)
        val parsed = parse(file, instance::class.java)
        parsed shouldBe instance
    }

    @Test
    fun `parse a file even if it has non-standard extension`() {
        val nonStandardFile = file.replaceExtension("fiz")
        write(nonStandardFile, format, instance)
        val parsed = parse(nonStandardFile, format, instance::class.java)
        parsed shouldBe instance
    }
}
