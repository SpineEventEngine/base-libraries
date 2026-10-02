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

import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Separator` should")
internal class SeparatorSpec {

    @Test
    fun `provide shortcut for system line separator`() {
        Separator.nl() shouldBe System.lineSeparator()
    }

    @Test
    fun `provide instance of 'Separator' which is system`() {
        Separator.system.value shouldBe System.lineSeparator()
    }

    @Test
    fun `obtain non-system line separators`() {
        Separator.nonSystem() shouldNotContain Separator.system
    }
}
