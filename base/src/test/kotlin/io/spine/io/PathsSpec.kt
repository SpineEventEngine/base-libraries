/*
 * Copyright 2024, TeamDev. All rights reserved.
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
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.spine.string.decodeBase64
import io.spine.testing.TestValues.randomString
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.pathString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Extensions for `Path` should")
internal class PathsSpec {

    @Test
    fun `obtain Base64-encoded path string`() {
        val original = Paths.get(randomString()) / randomString()
        val encoded = original.toBase64Encoded()
        val decoded = Paths.get(encoded.decodeBase64())
        
        decoded shouldBe original
    }

    @Test
    fun `replace file extension`() {
        Path("my/path/file.bin").replaceExtension(".txt") shouldBe Path("my/path/file.txt")
        Path("file").replaceExtension(".txt") shouldBe Path("file.txt")
        Path("file").replaceExtension("txt") shouldBe Path("file.txt")
        Path("file.txt").replaceExtension("") shouldBe Path("file")
        Path("file.").replaceExtension("") shouldBe Path("file")
    }

    @Test
    fun `provide current system path separator`() {
        Separator.system shouldBe File.separatorChar
    }

    @Test
    fun `provide extension for replacing Windows file separators`() {
        "C:\\Windows\\path".toUnix() shouldBe "C:/Windows/path"

        val path = "/my/unix/path"
        path.toUnix() shouldBeSameInstanceAs path
    }

    @Suppress("DEPRECATION")
    @Test
    fun `replace Windows separators in a path`() {
        Path("C:\\Windows\\path").toUnix() shouldBe Path("C:/Windows/path")
    }

    @Suppress("DEPRECATION")
    @Test
    fun `return the same path when no Windows separators present`() {
        val path = Path("/my/unix/path")
        path.toUnix() shouldBeSameInstanceAs path
    }
}
