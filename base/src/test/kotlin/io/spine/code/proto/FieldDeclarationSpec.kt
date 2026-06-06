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

import com.google.common.testing.EqualsTester
import com.google.common.testing.NullPointerTester
import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.BytesValue
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Empty
import com.google.protobuf.Int32Value
import com.google.protobuf.Int64Value
import com.google.protobuf.StringValue
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.test.code.proto.FieldContainer
import io.spine.test.code.proto.uuid.MttEntityState
import io.spine.test.type.Uri
import io.spine.test.type.Uri.Authorization
import io.spine.testing.Assertions.assertIllegalState
import io.spine.testing.nullPointerTester
import io.spine.testing.setDefault
import io.spine.testing.testAllPublicConstructors
import io.spine.type.MessageType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("TooManyFunctions")
@DisplayName("`FieldDeclaration` should")
internal class FieldDeclarationSpec {

    @Test
    fun `not accept 'null's on construction`() {
        val descriptor = Any.getDescriptor()
        nullPointerTester {
            setDefault(MessageType(descriptor))
            setDefault<FieldDescriptor>(descriptor.fields[0])
        }.testAllPublicConstructors<FieldDeclaration>()
    }

    @Test
    fun `handle 'null's in instance methods`() {
        val descriptor = Any.getDescriptor()
        val field = descriptor.fields[0]
        NullPointerTester()
            .testAllPublicInstanceMethods(FieldDeclaration(field))
    }

    @Test
    fun `have 'equals()' and 'hashCode()'`() {
        val any = Any.getDescriptor()
        val typeUrl = any.fields[0]
        val bytes = any.fields[1]
        EqualsTester()
            .addEqualityGroup(
                FieldDeclaration(typeUrl),
                FieldDeclaration(typeUrl, MessageType(any))
            )
            .addEqualityGroup(
                FieldDeclaration(bytes),
                FieldDeclaration(bytes, MessageType(any))
            )
            .testEquals()
    }

    @Nested internal inner class
    `check default values of type` {

        @Test
        fun int32() {
            val int32Field = Int32Value.getDescriptor().fields[0]
            val declaration = FieldDeclaration(int32Field)

            declaration.isDefault(0) shouldBe true
            declaration.isDefault("") shouldBe false
            declaration.isDefault(0.0) shouldBe false
        }

        @Test
        fun `string`() {
            val stringField = StringValue.getDescriptor().fields[0]
            val declaration = FieldDeclaration(stringField)

            declaration.isDefault("") shouldBe true
            declaration.isDefault(0L) shouldBe false
        }

        @Test
        fun `Message`() {
            val messageField = Uri.getDescriptor().findFieldByName("auth")
            val declaration = FieldDeclaration(messageField)

            declaration.isDefault(Authorization.getDefaultInstance()) shouldBe true
            declaration.isDefault(0L) shouldBe false
            declaration.isDefault(Empty.getDefaultInstance()) shouldBe false
        }
    }

    @Nested internal inner class
    `obtain Java type name of` {

        @Test
        fun int64() {
            val int64Field = Int64Value.getDescriptor().fields[0]
            val declaration = FieldDeclaration(int64Field)
            val typeName = declaration.javaTypeName()

            typeName shouldBe Long::class.javaPrimitiveType!!.name
        }

        @Test
        fun bytes() {
            val int64Field = BytesValue.getDescriptor().fields[0]
            val declaration = FieldDeclaration(int64Field)
            val typeName = declaration.javaTypeName()

            typeName shouldBe ByteString::class.java.canonicalName
        }

        @Test
        fun `Message`() {
            val messageField = Uri.getDescriptor().findFieldByName("protocol")
            val declaration = FieldDeclaration(messageField)
            val typeName = declaration.javaTypeName()

            typeName shouldBe Uri.Protocol::class.java.canonicalName
        }

        @Test
        fun `enum`() {
            val enumField = Uri.Protocol.getDescriptor().findFieldByName("schema")
            val declaration = FieldDeclaration(enumField)
            val typeName = declaration.javaTypeName()

            typeName shouldBe Uri.Schema::class.java.canonicalName
        }
    }

    @Test
    fun `obtain a name of the getter generated for the field by Protobuf Java`() {
        val field = Authorization.getDescriptor().findFieldByName("user_name")
        val declaration = FieldDeclaration(field)
        val javaGetterName = declaration.javaGetterName()

        javaGetterName shouldBe "getUserName"
    }

    @Test
    fun `obtain the message type of the field`() {
        val field = Uri.getDescriptor().findFieldByName("auth")
        val declaration = FieldDeclaration(field)
        val authorization = MessageType(Authorization.getDescriptor())

        declaration.messageType() shouldBe authorization
    }

    @Test
    fun `throw an 'ISE' if the field is of non-message type`() {
        val field = Authorization.getDescriptor().findFieldByName("user_name")
        val declaration = FieldDeclaration(field)

        assertIllegalState { declaration.messageType() }
    }

    @Test
    fun `tell if the field is a singular field of a message type`() {
        val field = Uri.getDescriptor().findFieldByName("auth")
        val declaration = FieldDeclaration(field)

        declaration.isSingularMessage shouldBe true
    }

    @Test
    fun `tell if the field is not a singular message type, if the field is repeated`() {
        val query = Uri.getDescriptor().findFieldByName("query")
        val nonSingular = FieldDeclaration(query)

        nonSingular.isSingularMessage shouldBe false
    }

    @Test
    fun `tell if the field is not a singular message type, if type is not message`() {
        val host = Uri.getDescriptor().findFieldByName("host")
        val nonMessage = FieldDeclaration(host)

        nonMessage.isSingularMessage shouldBe false
    }

    @Test
    fun `obtain Java type kind of the field`() {
        val host = Uri.getDescriptor().findFieldByName("host")
        val decl = FieldDeclaration(host)

        decl.javaType() shouldBe FieldDescriptor.JavaType.STRING
    }

    @Test
    fun `obtain field comments`() {
        val host = Uri.getDescriptor().findFieldByName("host")
        val decl = FieldDeclaration(host)

        decl.leadingComments() shouldBePresent {
            it shouldContain "Domain name."
        }
    }

    @Test
    fun `obtain the Protobuf field number`() {
        val mapField = FieldContainer.getDescriptor().findFieldByName("map_field")
        FieldDeclaration(mapField).number() shouldBe 3
    }

    @Nested inner class
    `tell the field type` {

        @Test
        fun scalar() {
            FieldDeclaration(field("primitive_field")).isScalar shouldBe true
            FieldDeclaration(field("message_field")).isScalar shouldBe false
        }

        @Test
        fun `string`() {
            FieldDeclaration(field("singular_field")).isString shouldBe true
        }

        @Test
        fun `enum`() {
            FieldDeclaration(field("enum_field")).isEnum shouldBe true
        }

        @Test
        fun message() {
            FieldDeclaration(field("message_field")).isMessage shouldBe true
        }

        @Test
        fun any() {
            FieldDeclaration(field("message_field")).isAny shouldBe true
            FieldDeclaration(Uri.getDescriptor().findFieldByName("auth")).isAny shouldBe false
        }
    }

    @Test
    fun `obtain the Java type name of a map value`() {
        val mapField = field("map_field")
        FieldDeclaration(mapField).javaTypeName() shouldBe String::class.java.name
    }

    @Test
    fun `obtain the value declaration of a map`() {
        val mapField = field("map_field")
        val valueDeclaration = FieldDeclaration(mapField).valueDeclaration()
        valueDeclaration.isString shouldBe true
    }

    @Nested inner class
    `obtain the class name` {

        @Test
        fun `of a scalar field`() {
            val className = FieldDeclaration(field("primitive_field")).className()
            className.value() shouldContain "Integer"
        }

        @Test
        fun `of a message field`() {
            val className = FieldDeclaration(field("message_field")).className()
            className.value() shouldContain "Any"
        }
    }

    @Nested inner class
    `tell if the field is an entity ID` {

        @Test
        fun `for the first field of an entity state`() {
            val idField = MttEntityState.getDescriptor().findFieldByName("uuid")
            FieldDeclaration(idField).isId shouldBe true
        }

        @Test
        fun `not for the first field of a non-entity message`() {
            FieldDeclaration(field("singular_field")).isId shouldBe false
        }

        @Test
        fun `not for a non-first field`() {
            FieldDeclaration(field("repeated_field")).isId shouldBe false
        }
    }

    private fun field(name: String): FieldDescriptor =
        FieldContainer.getDescriptor().findFieldByName(name)
}
