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

import io.kotest.matchers.shouldBe
import io.spine.environment.OsFamily.Unix
import io.spine.environment.OsFamily.Windows
import io.spine.environment.OsFamily.macOS
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("`OsFamily` should")
internal class OsFamilySpec {

    @Test
    fun `detect current OS`() {
        assertDoesNotThrow {
            OsFamily.detect()
        }
    }

    @Test
    fun `tell if it is not current`() {
        when (OsFamily.detect()) {
            Windows -> macOS.isCurrent shouldBe false
            macOS -> Unix.isCurrent shouldBe false
            Unix -> macOS.isCurrent shouldBe false
        }
    }
}
