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
