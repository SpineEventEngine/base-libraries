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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.testing.UtilityClassTest
import io.spine.util.Exceptions.illegalStateWithCauseOf
import io.spine.util.Exceptions.unsupported
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`Exceptions` utility class should")
internal class ExceptionsSpec : UtilityClassTest<Exceptions>(Exceptions::class.java) {

    @Test
    fun `throw 'UnsupportedOperationException' with a formatted message`() {
        val exception = assertThrows<UnsupportedOperationException> {
            unsupported("Cannot %s the %s.", "open", "door")
        }
        exception.message shouldBe "Cannot open the door."
    }

    @Test
    fun `wrap a cause into an 'IllegalStateException'`() {
        val cause = RuntimeException("Root failure.")

        val exception = assertThrows<IllegalStateException> {
            illegalStateWithCauseOf(cause)
        }

        exception.cause shouldBe cause
        exception.message shouldContain "Root failure."
    }
}
