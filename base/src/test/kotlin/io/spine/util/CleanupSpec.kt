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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.io.IOException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Cleanup` should")
internal class CleanupSpec {

    @Nested inner class
    `keep the first of two failures` {

        @Test
        fun `returning the next one if there is no first failure`() {
            val next = RuntimeException("next")
            val first: RuntimeException? = null

            first.withSuppressed(next) shouldBeSameInstanceAs next
            next.suppressed.shouldBeEmpty()
        }

        @Test
        fun `adding the next one to the first one as suppressed`() {
            val first = RuntimeException("first")
            val next = IllegalStateException("next")

            first.withSuppressed(next) shouldBeSameInstanceAs first
            first.suppressed.toList() shouldContainExactly listOf(next)
            next.suppressed.shouldBeEmpty()
        }

        @Test
        fun `not adding a failure to itself`() {
            val failure = RuntimeException("same")

            failure.withSuppressed(failure) shouldBeSameInstanceAs failure
            failure.suppressed.shouldBeEmpty()
        }

        @Test
        fun `adding later failures in the order they are passed`() {
            val first = RuntimeException("first")
            val second = AssertionError("second")
            val third = IllegalStateException("third")

            val result = first.withSuppressed(second).withSuppressed(third)

            result shouldBeSameInstanceAs first
            first.suppressed.toList() shouldContainExactly listOf(second, third)
        }
    }

    @Nested inner class
    `run all steps` {

        private val calls = mutableListOf<String>()

        private fun step(name: String, failure: Throwable? = null) = Runnable {
            calls += name
            failure?.let { throw it }
        }

        @Test
        fun `doing nothing if there are no steps`() {
            runAll()
        }

        @Test
        fun `in the order they are passed`() {
            runAll(step("first"), step("second"), step("third"))

            calls shouldContainExactly listOf("first", "second", "third")
        }

        @Test
        fun `accepting lambdas`() {
            runAll({ calls += "first" }, { calls += "second" })

            calls shouldContainExactly listOf("first", "second")
        }

        @Test
        fun `despite an earlier failure, throwing the first one`() {
            val first = RuntimeException("first")
            val third = IllegalStateException("third")

            val thrown = shouldThrow<RuntimeException> {
                runAll(step("first", first), step("second"), step("third", third))
            }

            thrown shouldBeSameInstanceAs first
            thrown.suppressed.toList() shouldContainExactly listOf(third)
            calls shouldContainExactly listOf("first", "second", "third")
        }

        @Test
        fun `despite an earlier 'Error'`() {
            val error = AssertionError("Failed by a test hook.")

            val thrown = shouldThrow<AssertionError> {
                runAll(step("first", error), step("second"))
            }

            thrown shouldBeSameInstanceAs error
            thrown.suppressed.shouldBeEmpty()
            calls shouldContainExactly listOf("first", "second")
        }

        @Test
        fun `adding later failures as suppressed in the order of steps`() {
            val first = IllegalStateException("first")
            val second = AssertionError("second")
            val third = IllegalArgumentException("third")

            val thrown = shouldThrow<IllegalStateException> {
                runAll(step("first", first), step("second", second), step("third", third))
            }

            thrown shouldBeSameInstanceAs first
            thrown.suppressed.toList() shouldContainExactly listOf(second, third)
        }

        @Test
        fun `not adding a failure to itself if steps throw the same instance`() {
            val failure = RuntimeException("shared")

            val thrown = shouldThrow<RuntimeException> {
                runAll(step("first", failure), step("second", failure), step("third"))
            }

            thrown shouldBeSameInstanceAs failure
            thrown.suppressed.shouldBeEmpty()
            calls shouldContainExactly listOf("first", "second", "third")
        }

        @Test
        fun `wrapping a checked exception into 'IllegalStateException'`() {
            val checked = IOException("checked")
            val later = RuntimeException("later")

            val thrown = shouldThrow<IllegalStateException> {
                runAll(step("first", checked), step("second", later))
            }

            thrown.cause shouldBeSameInstanceAs checked
            checked.suppressed.toList() shouldContainExactly listOf(later)
            thrown.suppressed.shouldBeEmpty()
        }

        @Test
        fun `restoring the interrupt status after all the steps`() {
            val first = RuntimeException("first")
            val interruption = InterruptedException("interrupted")
            var interruptedInLaterStep = true
            try {
                val thrown = shouldThrow<RuntimeException> {
                    runAll(
                        step("first", first),
                        step("second", interruption),
                        { interruptedInLaterStep = Thread.currentThread().isInterrupted }
                    )
                }

                thrown shouldBeSameInstanceAs first
                thrown.suppressed.toList() shouldContainExactly listOf(interruption)
                interruptedInLaterStep shouldBe false
                Thread.currentThread().isInterrupted shouldBe true
            } finally {
                // Clears the status, so that it does not leak to other tests.
                Thread.interrupted()
            }
        }
    }

    @Nested inner class
    `close all resources` {

        @Test
        fun `doing nothing if there are no resources`() {
            emptyList<AutoCloseable>().closeAll()
        }

        @Test
        fun `in the order of iteration`() {
            val closed = mutableListOf<String>()
            val resources = listOf("first", "second").map { name ->
                AutoCloseable { closed += name }
            }

            resources.closeAll()

            closed shouldContainExactly listOf("first", "second")
        }

        @Test
        fun `despite earlier failures, throwing the first one`() {
            val first = StubResource(RuntimeException("first"))
            val second = StubResource()
            val third = StubResource(AssertionError("third"))

            val thrown = shouldThrow<RuntimeException> {
                listOf(first, second, third).closeAll()
            }

            thrown shouldBeSameInstanceAs first.failure
            thrown.suppressed.toList() shouldContainExactly listOf(third.failure)
            listOf(first, second, third).forEach { it.closed shouldBe true }
        }

        @Test
        fun `wrapping a checked exception into 'IllegalStateException'`() {
            val resource = StubResource(Exception("checked"))

            val thrown = shouldThrow<IllegalStateException> {
                listOf(resource).closeAll()
            }

            thrown.cause shouldBeSameInstanceAs resource.failure
        }

        @Test
        fun `even if closing removes them from the iterable`() {
            val resources = mutableListOf<AutoCloseable>()
            val failure = RuntimeException("first")
            val second = StubResource()
            // Unregisters itself when closed, as a resource of a registry does.
            resources += AutoCloseable {
                resources.removeAt(0)
                throw failure
            }
            resources += second

            val thrown = shouldThrow<RuntimeException> {
                resources.closeAll()
            }

            thrown shouldBeSameInstanceAs failure
            second.closed shouldBe true
        }
    }

    /**
     * A resource which remembers that it was closed and then fails with
     * the given [failure], if any.
     */
    private class StubResource(val failure: Throwable? = null) : AutoCloseable {

        var closed = false
            private set

        override fun close() {
            closed = true
            failure?.let { throw it }
        }
    }
}
