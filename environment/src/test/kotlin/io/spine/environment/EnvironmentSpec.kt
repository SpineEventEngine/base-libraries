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

package io.spine.environment

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.environment.given.Staging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Environment` should")
internal class EnvironmentSpec {

    private lateinit var environment: Environment

    @BeforeEach
    fun setUp() {
        environment = Environment.instance()
        environment.reset()
    }

    @AfterEach
    fun cleanUp() {
        // Clear the callback installed on the shared `DefaultMode` singleton, then drop the
        // registered types and the testing system property.
        environment.whenDetected(DefaultMode::class.java, null)
        environment.reset()
        System.clearProperty(TestsProperty.KEY)
    }

    @Test
    fun `reject configuring a callback for an unregistered type`() {
        val exception = shouldThrow<IllegalArgumentException> {
            environment.whenDetected(Staging::class.java) { /* no-op */ }
        }
        exception.message shouldContain "was not registered"
    }

    @Test
    fun `stay in the same type when set to the current one again`() {
        environment.setTo(Tests::class.java)
        environment.setTo(Tests::class.java)

        environment.`is`(Tests::class.java) shouldBe true
    }

    @Test
    fun `pass the detected type instance to its callback`() {
        var detected: DefaultMode? = null
        environment.whenDetected(DefaultMode::class.java) { detected = it }

        // Disable the `Tests` type so that `DefaultMode` becomes the detected one.
        System.setProperty(TestsProperty.KEY, "false")
        environment.autoDetect()

        environment.type() shouldBe DefaultMode::class.java
        detected.shouldNotBeNull()
    }

    companion object {

        private lateinit var storedEnvironment: Environment

        @BeforeAll
        @JvmStatic
        fun storeEnvironment() {
            storedEnvironment = Environment.instance().createCopy()
        }

        @AfterAll
        @JvmStatic
        fun restoreEnvironment() {
            Environment.instance().restoreFrom(storedEnvironment)
        }
    }
}
