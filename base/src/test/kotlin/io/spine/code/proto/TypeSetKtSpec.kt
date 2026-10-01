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
