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

package io.spine.string

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Top-level functions in `Stringify.kt` should")
class StringifySpec {

    @Test
    fun `stringify an object`() {
        val value = 123
        value.stringify() shouldBe "123"
    }

    @Test
    fun `parse from string using type parameter`() {
        fromString<Int>("123") shouldBe 123
    }

    @Test
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "RemoveRedundantQualifierName")
    // To use `java.lang.Integer` instead of `kotlin.Int` for testing.
    fun `parse from string using 'KClass'`() {
        fromString("123", java.lang.Integer::class) shouldBe 123
    }

    @Test
    fun `create list stringifier`() {
        listStringifier<Int>().convert(listOf(1, 2)) shouldBe "\"1\",\"2\""
        listStringifier<Int>('#').convert(listOf(1, 2)) shouldBe "\"1\"#\"2\""
    }

    @Test
    fun `create map stringifier`() {
        mapStringifier<Int, String>().convert(mapOf(1 to "a")) shouldBe "\"1\":\"a\""
        mapStringifier<Int, String>('#').convert(mapOf(1 to "a", 2 to "b")) shouldBe
                "\"1\":\"a\"#\"2\":\"b\""
    }
}
