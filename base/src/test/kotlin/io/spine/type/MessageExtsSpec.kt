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
