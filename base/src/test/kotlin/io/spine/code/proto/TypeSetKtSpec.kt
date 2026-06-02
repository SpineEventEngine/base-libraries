/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.code.proto

import io.kotest.matchers.shouldBe
import io.spine.test.base.rejections.TestRejections.FlyingObjectUnidentified
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`TypeSet` should")
class TypeSetKtSpec {

    @Test
    fun `create an empty set`() {
        val set = TypeSet.newBuilder().build()
        set.isEmpty() shouldBe true
        set.size() shouldBe 0
    }

    @Test
    fun `create from a file descriptor`() {
        val descriptor = FlyingObjectUnidentified.getDescriptor().file
        val set = TypeSet.from(descriptor)

        set.isEmpty() shouldBe false
        set.messageTypes().size shouldBe 1
    }

    @Test
    fun `unite sets`() {
        val descriptor = FlyingObjectUnidentified.getDescriptor().file
        val set1 = TypeSet.from(descriptor)
        val set2 = TypeSet.newBuilder().build()
        val union = set1.union(set2)

        union shouldBe set1
    }
}
