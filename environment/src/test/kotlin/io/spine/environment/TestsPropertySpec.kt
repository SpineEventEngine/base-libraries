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

package io.spine.environment

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`TestsProperty` should")
internal class TestsPropertySpec {

    @AfterEach
    fun cleanUp() {
        System.clearProperty(TestsProperty.KEY)
    }

    @Test
    fun `throw when asked for the value while unset`() {
        System.clearProperty(TestsProperty.KEY)

        shouldThrow<IllegalStateException> {
            TestsProperty().value()
        }
    }

    @Test
    fun `recognize the values that mean we are under tests`() {
        TestsProperty.TESTS_VALUES.forEach { value ->
            System.setProperty(TestsProperty.KEY, value)
            TestsProperty().value() shouldBe true
        }
    }

    @Test
    fun `treat any other value as not under tests`() {
        System.setProperty(TestsProperty.KEY, "neitherTrueNor1")

        TestsProperty().value() shouldBe false
    }
}
