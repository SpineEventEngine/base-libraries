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

package io.spine.type

import io.kotest.matchers.shouldBe
import io.spine.given.type.ExplicitNonSpiService
import io.spine.given.type.ExplicitSpiService
import io.spine.given.type.ImplicitSpiService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`ServiceDescriptor` extensions in `io.spine.type` should")
internal class ServiceDescriptorExtsSpec {

    @Nested inner class
    `tell if a service is` {

        @Test
        fun `explicitly annotated SPI`() {
            ExplicitSpiService.getDescriptor().isSpi() shouldBe true
            ExplicitNonSpiService.getDescriptor().isSpi() shouldBe false
            ImplicitSpiService.getDescriptor().isSpi() shouldBe null
        }
    }
}
