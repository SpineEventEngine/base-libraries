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

package io.spine.compare

import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.util.UUID
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ComparatorRegistry` should`")
internal class ComparatorRegistrySpec {

    private val registry = ComparatorRegistry

    @Test
    fun `load the comparators from the providers`() {
        registry.contains(Timestamp::class.java) shouldBe true
        registry.contains(Duration::class.java) shouldBe true
    }

    @Test
    fun `register and check presence of comparators`() {
        val comparator = compareBy<String> { it.length }
        registry.contains<String>() shouldBe false
        registry.register<String>(comparator)
        registry.contains<String>() shouldBe true
    }

    @Test
    fun `override the already registered comparator`() {
        val comparator1 = compareBy<Double> { it }
        val comparator2 = compareBy<Double> { it }
        registry.register<Double>(comparator1)
        registry.register<Double>(comparator2)
        registry.get<Double>() shouldBe comparator2
    }

    @Test
    fun `return a comparator`() {
        val comparator = compareBy<Float> { it }
        shouldThrow<IllegalStateException> { registry.get<Float>() }
        registry.register<Float>(comparator)
        registry.get<Float>() shouldBe comparator
    }

    @Test
    fun `search for a comparator`() {
        val comparator = compareBy<StringBuilder> { it.length }
        registry.find<StringBuilder>() shouldBe null
        registry.register<StringBuilder>(comparator)
        registry.find<StringBuilder>() shouldBe comparator
    }

    @Test
    fun `expose the supported types`() {
        registry.types() shouldContain Timestamp::class.java
        registry.types() shouldContain Duration::class.java
    }

    @Test
    fun `include a newly registered type among the supported types`() {
        val comparator = compareBy<CharSequence> { it.length }
        registry.types() shouldNotContain CharSequence::class.java
        registry.register<CharSequence>(comparator)
        registry.types() shouldContain CharSequence::class.java
    }

    @Test
    fun `return a snapshot of the supported types`() {
        val snapshot = registry.types()
        snapshot shouldNotContain UUID::class.java
        registry.register<UUID>(compareBy { it.toString() })
        snapshot shouldNotContain UUID::class.java
    }
}
