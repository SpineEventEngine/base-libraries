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

import com.google.protobuf.ByteString
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.spine.testing.Assertions.assertIllegalState
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`ScalarType` should")
internal class ScalarTypeSpec {

    @Nested inner class
    `map a Protobuf scalar type to a Java type` {

        @Test
        fun `returning the Java class`() {
            ScalarType.javaType(Type.TYPE_INT32) shouldBe Integer.TYPE
            ScalarType.javaType(Type.TYPE_INT64) shouldBe java.lang.Long.TYPE
            ScalarType.javaType(Type.TYPE_BOOL) shouldBe java.lang.Boolean.TYPE
            ScalarType.javaType(Type.TYPE_STRING) shouldBe String::class.java
            ScalarType.javaType(Type.TYPE_BYTES) shouldBe ByteString::class.java
        }

        @Test
        fun `returning the Java type name`() {
            ScalarType.javaTypeName(Type.TYPE_STRING) shouldBe String::class.java.name
            ScalarType.javaTypeName(Type.TYPE_DOUBLE) shouldBe "double"
        }

        @Test
        fun `rejecting a non-scalar type`() {
            assertIllegalState { ScalarType.javaType(Type.TYPE_GROUP) }
        }
    }

    @Nested inner class
    `tell if a field has a scalar type` {

        @Test
        fun `recognizing a scalar field`() {
            val field = fieldOfType(Type.TYPE_INT64)
            ScalarType.isScalarType(field) shouldBe true
            ScalarType.of(field) shouldBePresent { it shouldBe ScalarType.INT64 }
        }

        @Test
        fun `rejecting a non-scalar field`() {
            val field = fieldOfType(Type.TYPE_MESSAGE)
            ScalarType.isScalarType(field) shouldBe false
            ScalarType.of(field).isPresent shouldBe false
        }
    }

    @Test
    fun `expose the corresponding Protobuf and Java types`() {
        ScalarType.STRING.protoScalarType() shouldBe Type.TYPE_STRING
        ScalarType.STRING.javaClass() shouldBe String::class.java
        ScalarType.BYTES.javaClass() shouldBe ByteString::class.java
    }
}

private fun fieldOfType(type: Type): FieldDescriptorProto =
    FieldDescriptorProto.newBuilder()
        .setName("test_field")
        .setType(type)
        .build()
