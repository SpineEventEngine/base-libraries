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

package io.spine.security

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

@DisplayName("`InvocationGuard` should")
internal class InvocationGuardTest {

    @Nested internal inner class
    `throw 'SecurityException'` {

        @Test
        fun `if no classes are allowed`() {
            assertThrowsOn { InvocationGuard.allowOnly("") }
        }

        @Test
        fun `if a calling class is not that allowed`() {
            assertThrowsOn { InvocationGuard.allowOnly("java.lang.Boolean") }
        }

        @Test
        fun `if a calling class is not among allowed`() {
            assertThrowsOn {
                InvocationGuard.allowOnly(
                    "java.lang.String",
                    "org.junit.jupiter.api.Test"
                )
            }
        }
    }

    @Test
    fun `do not throw on allowed class`() {
        val callingClass = CallerProvider.callerClass().getName()
        try {
            InvocationGuard.allowOnly(callingClass)
        } catch (e: Exception) {
            fail<Any>(e)
        }
    }

    private fun assertThrowsOn(executable: Executable) {
        assertThrows(SecurityException::class.java, executable)
    }
}
