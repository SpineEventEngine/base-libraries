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
import org.junit.jupiter.api.Test

@DisplayName("`Field` should append nested paths")
internal class FieldNestingSpec {

    @Test
    fun `by appending the path of another field`() {
        val parent = Field.named("road_to")
        val other = Field.parse("mandalay.bay")

        val combined = parent.nested(other)

        combined.toString() shouldBe "road_to.mandalay.bay"
    }

    @Test
    fun `by appending a single-segment field`() {
        val parent = Field.parse("a.b")
        val other = Field.named("c")

        parent.nested(other).toString() shouldBe "a.b.c"
    }
}
