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

package io.spine.code.fs

import com.google.common.collect.ImmutableList
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`AbstractSourceFile` should")
class AbstractSourceFileKtSpec {

    private class StubSourceFile(path: Path) : AbstractSourceFile(path) {
        public override fun load() = super.load()
        public override fun store() = super.store()
        public fun setLines(lines: List<String>) = update(ImmutableList.copyOf(lines))
        public fun getLines() = lines()
    }

    @Test
    fun `return empty lines if not loaded`(@TempDir tempDir: Path) {
        val file = StubSourceFile(tempDir.resolve("non-existent"))
        file.getLines().shouldBeEmpty()
    }

    @Test
    fun `load lines from file`(@TempDir tempDir: Path) {
        val path = tempDir.resolve("test.txt")
        val content = listOf("line 1", "line 2")
        Files.write(path, content)

        val file = StubSourceFile(path)
        file.load()
        file.getLines() shouldContainExactly content
    }

    @Test
    fun `store lines to file`(@TempDir tempDir: Path) {
        val path = tempDir.resolve("test.txt")
        Files.createFile(path)
        val content = listOf("line 1", "line 2")

        val file = StubSourceFile(path)
        file.setLines(content)
        file.store()

        Files.readAllLines(path) shouldContainExactly content
    }
}
