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
import io.spine.string.decodeBase64
import io.spine.testing.TestValues.randomString
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.div
import org.junit.jupiter.api.DisplayName
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
    fun `convert 'Path' to a Unix-style string`() {
        Path("my\\windows\\path").toUnixPath() shouldBe "my/windows/path"
        Path("my/unix/path").toUnixPath() shouldBe "my/unix/path"
    }
}
