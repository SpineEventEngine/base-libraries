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

package io.spine.protobuf

import io.kotest.matchers.shouldBe
import io.spine.logging.testing.tapConsole
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Protobuf string functions should")
internal class ProtobufLiteralSpec {

    @Test
    fun `restore escaped ASCII control characters`() {
        // The test string contains a mix of control characters and English letters.
        val asciiCodes = listOf(7, 8, 101, 102, 9, 10, 11, 72, 73, 12, 13, 111, 34, 39, 92)
        val asciiString = asciiCodes.map { it.toChar() }.joinToString()

        val expected = "\\a, \\b, e, f, \\t, \\n, \\v, H, I, \\f, \\r, o, \\\", \\', \\\\"
        val result = tapConsole {
            // With restored escape sequences, the test string becomes printable.
            val escaped = restoreProtobufEscapes(asciiString)
            print(escaped)
        }

        result shouldBe expected
    }
}
