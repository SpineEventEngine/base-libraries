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
