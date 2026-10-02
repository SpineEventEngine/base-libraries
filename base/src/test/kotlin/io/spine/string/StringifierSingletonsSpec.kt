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

import com.google.common.testing.SerializableTester.reserialize
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Built-in stringifiers should resolve to their singleton on deserialization")
internal class StringifierSingletonsSpec {

    @Test
    fun `Long stringifier`() {
        reserialize(LongStringifier.getInstance()) shouldBeSameInstanceAs
                LongStringifier.getInstance()
    }

    @Test
    fun `Integer stringifier`() {
        reserialize(IntegerStringifier.getInstance()) shouldBeSameInstanceAs
                IntegerStringifier.getInstance()
    }

    @Test
    fun `Boolean stringifier`() {
        reserialize(BooleanStringifier.getInstance()) shouldBeSameInstanceAs
                BooleanStringifier.getInstance()
    }

    @Test
    fun `no-op stringifier`() {
        reserialize(NoOpStringifier.getInstance()) shouldBeSameInstanceAs
                NoOpStringifier.getInstance()
    }
}
