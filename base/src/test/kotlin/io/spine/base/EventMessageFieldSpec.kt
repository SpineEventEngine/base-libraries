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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.spine.testing.TestValues
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`EventMessageField` should")
class EventMessageFieldSpec {

    @Test
    fun `keep a passed field`() {
        val source = Field.named("id")

        val eventField = EventMessageField(source)

        (eventField.field === source) shouldBe true
    }

    @Test
    fun `reject a null field`() {
        shouldThrow<NullPointerException> {
            EventMessageField(TestValues.nullRef())
        }
    }
}
