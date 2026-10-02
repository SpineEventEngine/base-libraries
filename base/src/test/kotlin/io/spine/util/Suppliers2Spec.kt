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

package io.spine.util

import io.kotest.matchers.shouldBe
import io.spine.testing.UtilityClassTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Suppliers2` utility class should")
internal class Suppliers2Spec : UtilityClassTest<Suppliers2>(Suppliers2::class.java) {

    @Test
    fun `return the value obtained from the delegate`() {
        val memoized = Suppliers2.memoize { "answer" }

        memoized.get() shouldBe "answer"
    }

    @Test
    fun `call the delegate lazily and only once`() {
        var calls = 0
        val memoized = Suppliers2.memoize { ++calls }

        calls shouldBe 0

        memoized.get() shouldBe 1
        memoized.get() shouldBe 1
        calls shouldBe 1
    }
}
