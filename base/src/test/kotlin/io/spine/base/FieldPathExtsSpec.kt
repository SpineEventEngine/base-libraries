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

package io.spine.base

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Extensions for `FieldPath` should")
internal class FieldPathExtsSpec {

    @Nested inner class
    prohibit {

        @Test
        fun `empty paths`() {
            assertIllegal("")
        }

        @Test
        fun `paths not matching the pattern`() {
            assertIllegal("foo/bar")
            assertIllegal(" f o o")
            assertIllegal("0chance")
            assertIllegal(".pasaran")
        }

        private fun assertIllegal(path: String) {
            assertThrows<IllegalArgumentException> {
                FieldPath(path)
            }
        }
    }

    @Test
    fun `tell if the path is nested or not`() {
        FieldPath("fiz.b_z").isNotNested shouldBe false
        FieldPath("top").isNotNested shouldBe true
    }

    @Test
    fun `join into string`() {
        val path = "big.bada.boom"
        FieldPath(path).joined shouldBe path

        val notNested = "plain"
        FieldPath(notNested).joined shouldBe notNested
    }

    @Test
    fun `obtain the top path component`() {
        FieldPath("going.down").root shouldBe "going"
    }

    @Test
    fun `obtain a nested path`() {
        FieldPath("from.top_to.bottom").stepInto() shouldBe FieldPath("top_to.bottom")
        assertThrows<IllegalStateException> {
            FieldPath("top").stepInto()
        }
    }
}
