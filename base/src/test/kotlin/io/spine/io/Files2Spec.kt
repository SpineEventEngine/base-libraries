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
