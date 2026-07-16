/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
