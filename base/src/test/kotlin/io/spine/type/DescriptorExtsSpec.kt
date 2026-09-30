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
import io.spine.given.type.ExplicitBetaType
import io.spine.given.type.ExplicitExperimentalType
import io.spine.given.type.ExplicitInternalType
import io.spine.given.type.ExplicitNonBetaType
import io.spine.given.type.ExplicitNonExperimentalType
import io.spine.given.type.ExplicitNonInternalType
import io.spine.given.type.ExplicitNonSpiType
import io.spine.given.type.ExplicitSpiType
import io.spine.given.type.ImplicitBetaType
import io.spine.given.type.ImplicitExperimentalType
import io.spine.given.type.ImplicitInternalType
import io.spine.given.type.ImplicitSpiType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Descriptor` extensions from `io.spine.type` should")
internal class DescriptorExtsSpec {

    @Nested inner class
    `tell that a message type is marked as` {

        @Test
        fun beta() {
            ExplicitBetaType.getDescriptor().isBeta() shouldBe true
            ExplicitNonBetaType.getDescriptor().isBeta() shouldBe false
            ImplicitBetaType.getDescriptor().isBeta() shouldBe null
        }

        @Test
        fun experimental() {
            ExplicitExperimentalType.getDescriptor().isExperimental() shouldBe true
            ExplicitNonExperimentalType.getDescriptor().isExperimental() shouldBe false
            ImplicitExperimentalType.getDescriptor().isExperimental() shouldBe null
        }

        @Test
        fun internal() {
            ExplicitInternalType.getDescriptor().isInternal() shouldBe true
            ExplicitNonInternalType.getDescriptor().isInternal() shouldBe false
            ImplicitInternalType.getDescriptor().isInternal() shouldBe null
        }

        @Test
        fun spi() {
            ExplicitSpiType.getDescriptor().isSpi() shouldBe true
            ExplicitNonSpiType.getDescriptor().isSpi() shouldBe false
            ImplicitSpiType.getDescriptor().isSpi() shouldBe null
        }
    }
}
