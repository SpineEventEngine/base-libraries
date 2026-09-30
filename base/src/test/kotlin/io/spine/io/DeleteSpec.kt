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
import java.nio.file.Files
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Delete` should")
internal class DeleteSpec {

    @Test
    fun `recursively delete a directory`() {
        val directory = Files.createTempDirectory("delete-spec")
        Files.createFile(directory.resolve("file.txt"))

        deleteRecursively(directory) shouldBe true
        Files.exists(directory) shouldBe false
    }

    @Test
    fun `register a shutdown hook for recursive deletion`() {
        val directory = Files.createTempDirectory("delete-on-shutdown")
        // Registering the hook should not throw. The deletion itself runs at JVM exit.
        deleteRecursivelyOnShutdownHook(directory)
    }
}
