/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

@DisplayName("`FileDescriptor` extensions from `io.spine.type` should")
internal class FileDescriptorExtsSpec {

    @Nested inner class
    `tell if all types in a file are` {

        @Test
        fun beta() {
            ImplicitBetaType.getDescriptor().file.allTypesAreBeta() shouldBe true
            // The file itself is non-beta, but it's declared in the file with `beta_all`.
            ExplicitNonBetaType.getDescriptor().file.allTypesAreBeta() shouldBe true
            ExplicitBetaType.getDescriptor().file.allTypesAreBeta() shouldBe null
        }

        @Test
        fun internal() {
            ImplicitInternalType.getDescriptor().file.allTypesAreInternal() shouldBe true
            // The file itself is non-internal, but it's declared in the file with `internal_all`.
            ExplicitNonInternalType.getDescriptor().file.allTypesAreInternal() shouldBe true
            ExplicitInternalType.getDescriptor().file.allTypesAreInternal() shouldBe null
        }

        @Test
        fun spi() {
            ImplicitSpiType.getDescriptor().file.allTypesAreSpi() shouldBe true
            // The file itself is non-SPI, but it's declared in the file with `SPI_all`.
            ExplicitNonSpiType.getDescriptor().file.allTypesAreSpi() shouldBe true
            ExplicitSpiType.getDescriptor().file.allTypesAreSpi() shouldBe null
        }

        @Test
        fun experimental() {
            ImplicitExperimentalType.getDescriptor().file.allTypesAreExperimental() shouldBe true
            // The file itself is non-experimental, but it's declared in the file
            // with `experimental_all`.
            ExplicitNonExperimentalType.getDescriptor().file.allTypesAreExperimental() shouldBe true
            ExplicitExperimentalType.getDescriptor().file.allTypesAreExperimental() shouldBe null
        }
    }
}
