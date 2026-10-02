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

    @Nested inner class
    `check if field` {

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
