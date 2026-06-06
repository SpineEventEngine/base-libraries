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
