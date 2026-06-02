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
import io.kotest.matchers.shouldBe
import io.spine.testing.UtilityClassTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Math2` should")
class Math2Spec : UtilityClassTest<Math2>(Math2::class.java) {

    @Test
    fun `multiply long by int`() {
        Math2.safeMultiply(10L, 2) shouldBe 20L
        Math2.safeMultiply(10L, 0) shouldBe 0L
        Math2.safeMultiply(10L, 1) shouldBe 10L
        Math2.safeMultiply(10L, -1) shouldBe -10L
        Math2.safeMultiply(Long.MAX_VALUE, 1) shouldBe Long.MAX_VALUE
    }

    @Test
    fun `fail to multiply on overflow`() {
        shouldThrow<ArithmeticException> {
            Math2.safeMultiply(Long.MAX_VALUE, 2)
        }
        shouldThrow<ArithmeticException> {
            Math2.safeMultiply(Long.MIN_VALUE, -1)
        }
    }

    @Test
    fun `perform floor division`() {
        Math2.floorDiv(0, 4) shouldBe 0L
        Math2.floorDiv(-1, 4) shouldBe -1L
        Math2.floorDiv(-2, 4) shouldBe -1L
        Math2.floorDiv(-3, 4) shouldBe -1L
        Math2.floorDiv(-4, 4) shouldBe -1L
        Math2.floorDiv(-5, 4) shouldBe -2L
        Math2.floorDiv(4, 4) shouldBe 1L
        Math2.floorDiv(5, 4) shouldBe 1L
    }
}
