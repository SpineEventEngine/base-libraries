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

import com.google.protobuf.StringValue
import io.kotest.matchers.shouldBe
import io.spine.annotation.Internal
import io.spine.given.type.ExplicitInternalType
import io.spine.given.type.ImplicitInternalType
import io.spine.testing.StubMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Extensions for `Message` in `io.spine.type` package should")
internal class MessageExtsSpec {

    @Test
    fun `provide type name of a message`() {
        ExplicitInternalType.getDefaultInstance().typeName.value shouldBe
                "spine.given.type.ExplicitInternalType"
    }

    @Nested inner class
    `tell if a message is internal` {

        @Test
        fun `by class annotation`() {
            StubInternalMessage().isInternal() shouldBe true
        }

        @Test
        fun `by explicit type option, if class is not annotated`() {
            ExplicitInternalType.getDefaultInstance().isInternal() shouldBe true
        }

        @Test
        fun `by taking the 'internal_all' option from the declaring file`() {
            ImplicitInternalType.getDefaultInstance().isInternal() shouldBe true
        }
    }

    @Test
    fun `tell if a message class is internal`() {
        ExplicitInternalType::class.java.isInternal() shouldBe true
        ImplicitInternalType::class.java.isInternal() shouldBe true
        StringValue::class.java.isInternal() shouldBe false
    }
}

@Internal
private class StubInternalMessage: StubMessage()
