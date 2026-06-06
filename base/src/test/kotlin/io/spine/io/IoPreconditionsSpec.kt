/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import io.spine.io.IoPreconditions.checkExists
import io.spine.io.IoPreconditions.checkIsDirectory
import io.spine.io.IoPreconditions.checkNotDirectory
import io.spine.testing.Assertions.assertIllegalArgument
import io.spine.testing.UtilityClassTest
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`IoPreconditions` should")
internal class IoPreconditionsSpec :
    UtilityClassTest<IoPreconditions>(IoPreconditions::class.java) {

    @Nested inner class
    `check that a path exists` {

        @Test
        fun `returning an existing path`(@TempDir dir: Path) {
            val file = Files.createFile(dir.resolve("present.txt"))
            checkExists(file) shouldBe file
        }

        @Test
        fun `rejecting a missing path`(@TempDir dir: Path) {
            val missing = dir.resolve("absent.txt")
            assertIllegalArgument { checkExists(missing) }
        }
    }

    @Nested inner class
    `check that a file is not a directory` {

        @Test
        fun `returning a regular file`(@TempDir dir: Path) {
            val file = Files.createFile(dir.resolve("regular.txt")).toFile()
            checkNotDirectory(file) shouldBe file
        }

        @Test
        fun `rejecting an existing directory`(@TempDir dir: Path) {
            assertIllegalArgument { checkNotDirectory(dir.toFile()) }
        }
    }

    @Nested inner class
    `check that a path is a directory` {

        @Test
        fun `returning a directory`(@TempDir dir: Path) {
            checkIsDirectory(dir) shouldBe dir
        }

        @Test
        fun `rejecting a non-directory path`(@TempDir dir: Path) {
            val file = Files.createFile(dir.resolve("file.txt"))
            assertIllegalArgument { checkIsDirectory(file) }
        }
    }
}
