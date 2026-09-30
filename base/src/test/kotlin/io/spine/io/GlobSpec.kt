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

import com.google.common.truth.Truth.assertWithMessage
import io.kotest.matchers.shouldBe
import java.nio.file.Paths
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`Glob` should")
class GlobSpec {

    @Test
    fun `prohibit empty pattern`() {
        assertThrows<IllegalArgumentException> { Glob("") }
    }

    @Nested inner class
    `create instances by extension which` {

        @Test
        fun `is empty`() {
            assertExtensionMatches("", "some/where/file.")
        }

        @Test
        fun `has leading dot`() {
            assertExtensionMatches(".foo", "1/2/3/file.foo")
        }

        @Test
        fun `is just text`() {
            assertExtensionMatches("bar", "4/5/file.bar")
        }

        private fun assertExtensionMatches(extension: String, path: String) {
            val g = Glob.extension(extension)
            val p = Paths.get(path)
            val matches = g.matches(p)

            matches shouldBe true
        }
    }

    @Test
    fun `allow both lowercase and uppercase values`() {
        val g = Glob.extensionLowerAndUpper("hey", "jude", "mIx")

        fun assertMatches(file: String) {
            val p = Paths.get(file)
            assertWithMessage("The file `%s` should match the pattern `%s`.", file, g.pattern)
                .that(g.matches(p))
                .isTrue()
        }
        
        fun assertDoesNotMatch(file: String) {
            val p = Paths.get(file)
            assertWithMessage("The file `%s` should NOT match the pattern `%s`.", file, g.pattern)
                .that(g.matches(p))
                .isFalse()
        }

        assertMatches("1.hey")
        assertMatches("2.HEY")
        assertMatches("3.jude")
        assertMatches("4.JUDE")
        assertMatches("5.mix")
        assertMatches("6.MIX")

        assertDoesNotMatch("hey")
        assertDoesNotMatch("jude.")
        assertDoesNotMatch("mix")
        assertDoesNotMatch("mIx")
        assertDoesNotMatch("miX")
    }

    @Test
    fun `create pattern matching files without extensions`() {
        val noExtensions = Glob.extension()
        val p = Paths.get("my_file.")

        noExtensions.matches(p) shouldBe true
    }
}
