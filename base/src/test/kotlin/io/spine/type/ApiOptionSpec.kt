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

import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.spine.given.type.ExplicitBetaType
import io.spine.given.type.ExplicitExperimentalType
import io.spine.given.type.ExplicitNonInternalType
import io.spine.given.type.ExplicitInternalType
import io.spine.given.type.ExplicitNonBetaType
import io.spine.given.type.ExplicitNonSpiType
import io.spine.given.type.ExplicitSpiService
import io.spine.given.type.ExplicitSpiType
import io.spine.given.type.Filderation
import io.spine.given.type.ImplicitBetaType
import io.spine.given.type.ImplicitExperimentalType
import io.spine.given.type.ImplicitInternalType
import io.spine.given.type.ImplicitSpiType
import io.spine.protobuf.field
import java.util.*
import java.util.Optional.empty
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`ApiOption` should")
class ApiOptionSpec {

    @Nested inner class
    `provide items for API stability annotations` {

        @Test
        fun beta() {
            val beta = ApiOption.beta()
            // We do not allow annotating services as `beta`.
            beta.supportsServices() shouldBe false
            beta.supportsFields() shouldBe true
        }

        @Test
        fun spi() {
            val spi = ApiOption.spi()
            spi.supportsServices() shouldBe true
            spi.supportsFields() shouldBe false
        }

        @Test
        fun experimental() {
            val experimental = ApiOption.experimental()
            experimental.supportsServices() shouldBe false
            experimental.supportsFields() shouldBe true
        }

        @Test
        fun internal() {
            val internal = ApiOption.internal()
            internal.supportsServices() shouldBe false
            internal.supportsFields() shouldBe true
        }
    }

    @Nested inner class
    `locate itself in descriptors` {

        @Test
        fun beta() {
            val beta = ApiOption.beta()

            beta.findIn(ExplicitBetaType.getDescriptor()) shouldBePresent {
                it shouldBe true
            }

            val implicitBeta = ImplicitBetaType.getDescriptor()
            beta.findIn(implicitBeta) shouldBe empty()
            beta.findIn(implicitBeta.file) shouldBePresent {
                it shouldBe true
            }

            beta.findIn(ExplicitNonBetaType.getDescriptor()) shouldBePresent {
                it shouldBe false
            }

            val d = Filderation.getDescriptor()

            beta.findIn(d.field("beta_field")!!) shouldBePresent {
                it shouldBe true
            }
            beta.findIn(d.field("non_beta")!!) shouldBe empty()
            beta.findIn(d.field("explicit_non_beta")!!) shouldBePresent {
                it shouldBe false
            }
        }

        @Test
        fun spi() {
            val spi = ApiOption.spi()

            spi.findIn(ExplicitSpiType.getDescriptor()) shouldBePresent {
                it shouldBe true
            }

            val implicitSpi = ImplicitSpiType.getDescriptor()
            spi.findIn(implicitSpi) shouldBe empty()
            spi.findIn(implicitSpi.file) shouldBePresent {
                it shouldBe true
            }

            spi.findIn(ExplicitNonSpiType.getDescriptor()) shouldBePresent {
                it shouldBe false
            }

            spi.findIn(ExplicitSpiService.getDescriptor()) shouldBePresent {
                it shouldBe true
            }
        }

        @Test
        fun experimental() {
            val experimental = ApiOption.experimental()

            experimental.findIn(ExplicitExperimentalType.getDescriptor()) shouldBePresent {
                it shouldBe true
            }

            val implicitExperimental = ImplicitExperimentalType.getDescriptor()
            experimental.findIn(implicitExperimental) shouldBe empty()
            experimental.findIn(implicitExperimental.file) shouldBePresent {
                it shouldBe true
            }

            val d = Filderation.getDescriptor()

            experimental.findIn(d.field("experimental_field")!!) shouldBePresent {
                it shouldBe true
            }
            experimental.findIn(d.field("non_experimental")!!) shouldBe empty()
            experimental.findIn(d.field("explicit_non_experimental")!!) shouldBePresent {
                it shouldBe false
            }
        }

        @Test
        fun internal() {
            val internal = ApiOption.internal()

            internal.findIn(ExplicitInternalType.getDescriptor()) shouldBePresent {
                it shouldBe true
            }
            internal.findIn(ImplicitInternalType.getDescriptor()) shouldBe empty()
            internal.findIn(ExplicitNonInternalType.getDescriptor()) shouldBePresent {
                it shouldBe false
            }

            val d = Filderation.getDescriptor()

            internal.findIn(d.field("internal_field")!!) shouldBePresent {
                it shouldBe true
            }
            internal.findIn(d.field("non_internal")!!) shouldBe empty()
            internal.findIn(d.field("explicit_non_internal")!!) shouldBePresent {
                it shouldBe false
            }
        }
    }

    @Test
    fun `obtain string form as the name of the message option field`() {
        ApiOption.beta().toString() shouldBe "beta_type"
    }
}
