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
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.io.File
import kotlin.io.path.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Extensions for `File` should")
internal class FilesSpec {

    @Test
    fun `replace file extension`() {
        File("my/path/file.bin").replaceExtension(".txt") shouldBe File("my/path/file.txt")
        File("file").replaceExtension(".txt") shouldBe File("file.txt")
        File("file").replaceExtension("txt") shouldBe File("file.txt")
        File("file.txt").replaceExtension("") shouldBe File("file")
        File("file.").replaceExtension("") shouldBe File("file")
    }

    @Test
    fun `convert to Unix-style path`() {
        File("my\\windows\\path").toUnixPath() shouldBe "my/windows/path"
        File("my/unix/path").toUnixPath() shouldBe "my/unix/path"
    }
}
