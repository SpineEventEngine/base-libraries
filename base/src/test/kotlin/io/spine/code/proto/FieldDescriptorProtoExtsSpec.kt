/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.code.proto

import io.kotest.matchers.shouldBe
import io.spine.code.proto.given.Given.enumField
import io.spine.code.proto.given.Given.mapField
import io.spine.code.proto.given.Given.messageField
import io.spine.code.proto.given.Given.primitiveField
import io.spine.code.proto.given.Given.repeatedField
import io.spine.code.proto.given.Given.singularField
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`FieldDescriptorProto` extensions should")
internal class FieldDescriptorProtoExtsSpec {

    @Nested
    @DisplayName("check if field")
    inner class CheckIfField {

        @Test
        @DisplayName("is message")
        fun isMessage() {
            messageField().toProto().isMessage() shouldBe true
            primitiveField().toProto().isMessage() shouldBe false
            enumField().toProto().isMessage() shouldBe false
        }

        @Test
        @DisplayName("is repeated")
        fun isRepeated() {
            repeatedField().toProto().isRepeated() shouldBe true
            mapField().toProto().isRepeated() shouldBe false
            singularField().toProto().isRepeated() shouldBe false
        }

        @Test
        @DisplayName("is map")
        fun isMap() {
            mapField().toProto().isMap() shouldBe true
            singularField().toProto().isMap() shouldBe false
        }
    }

    @Test
    @DisplayName("obtain a map entry name")
    fun obtainEntryName() {
        mapField().toProto().entryName() shouldBe "MapFieldEntry"
    }
}
