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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.spine.testing.UtilityClassTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Iterators2` utility class should")
internal class Iterators2Spec : UtilityClassTest<Iterators2>(Iterators2::class.java) {

    @Test
    fun `retain only the elements matching the predicate`() {
        val source = listOf(1, 2, 3, 4, 5, 6).iterator()

        val filtered = Iterators2.filter(source) { it % 2 == 0 }

        filtered.asSequence().toList() shouldContainExactly listOf(2, 4, 6)
    }

    @Test
    fun `return an exhausted iterator when no elements match`() {
        val source = listOf(1, 3, 5).iterator()

        val filtered = Iterators2.filter(source) { it % 2 == 0 }

        filtered.hasNext() shouldBe false
    }

    @Test
    fun `not support removal`() {
        val source = mutableListOf(1, 2, 3)
        val filtered: MutableIterator<Int> = Iterators2.filter(source.iterator()) { true }
        filtered.next()

        shouldThrow<UnsupportedOperationException> {
            filtered.remove()
        }
    }
}
