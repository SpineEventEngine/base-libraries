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

package io.spine.collect

import io.kotest.matchers.shouldBe
import java.util.stream.Stream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("Extensions for `Iterable` should")
class MoreIterablesSpec {

    @Test
    fun `obtain the only element of a collection`() {
        val list = listOf(42)
        list.theOnly() shouldBe 42
    }

    @Test
    fun `fail to obtain the only element if collection is empty`() {
        val set = setOf<Any>()
        assertThrows<NoSuchElementException> { set.theOnly() }
    }

    @Test
    fun `fail to obtain the only element if collection has many elements`() {
        val set = setOf<Any>("foo", "bar")
        assertThrows<IllegalArgumentException> { set.theOnly() }
    }

    @ParameterizedTest
    @MethodSource("interlaceCollections")
    fun `interlace a collection`(elements: List<Any>, separator: Any, expected: List<Any>) {
        elements.interlaced(separator).toList() shouldBe expected
    }

    companion object {

        @Suppress("unused") // Used by JUnit.
        @JvmStatic
        fun interlaceCollections(): Stream<Arguments> = Stream.of(
            Arguments.arguments(listOf(0, 1, 2), 42, listOf(0, 42, 1, 42, 2)),
            Arguments.arguments(
                listOf("sea", "Moon", "Earth", "Sun"),
                "of",
                listOf("sea", "of", "Moon", "of", "Earth", "of", "Sun")
            ),
            Arguments.arguments(listOf<String>(), "doesn't matter", listOf<String>()),
        )
    }
}
