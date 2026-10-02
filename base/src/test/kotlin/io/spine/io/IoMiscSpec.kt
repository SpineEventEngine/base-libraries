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
import java.nio.file.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`io` package members should")
internal class IoMiscSpec {

    @Test
    fun `obtain the system temporary directory`() {
        Files2.systemTempDir().isNotEmpty() shouldBe true
    }

    @Test
    fun `build a glob matching lower- and upper-case extensions`() {
        val glob = Glob.extensionLowerAndUpper(listOf("txt"))

        glob.matches(Path.of("file.txt")) shouldBe true
        glob.matches(Path.of("file.TXT")) shouldBe true
        glob.matches(Path.of("file.md")) shouldBe false
    }
}
