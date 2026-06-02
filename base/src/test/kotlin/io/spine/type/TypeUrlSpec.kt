/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.type

import com.google.common.testing.EqualsTester
import com.google.common.testing.SerializableTester
import com.google.protobuf.Any
import com.google.protobuf.any
import com.google.protobuf.AnyProto
import com.google.protobuf.BoolValue
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.EnumDescriptor
import com.google.protobuf.Descriptors.GenericDescriptor
import com.google.protobuf.Field
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.StringValue
import com.google.protobuf.Timestamp
import com.google.protobuf.UInt32Value
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import io.spine.base.Identifier
import io.spine.option.EntityOption
import io.spine.protobuf.TypeConverter
import io.spine.protobuf.field
import io.spine.test.type.GreetingService
import io.spine.test.type.Transmission
import io.spine.test.type.TypeWithoutPrefix
import io.spine.test.type.Uri
import io.spine.testing.Assertions.assertIllegalArgument
import io.spine.testing.Assertions.assertNpe
import io.spine.testing.TestValues
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`TypeUrl` should")
internal class TypeUrlSpec {

    /** The descriptor of the type used for the tests.  */
    private val descriptor: Descriptor = StringValue.getDescriptor()

    /** Full proto type name for the type used under the tests.  */
    private val typeName: String = descriptor.fullName

    /** The value of the Type URL.  */
    private val typeUrlValue: String =
        TypeUrl.composeTypeUrl(TypeUrl.Prefix.GOOGLE_APIS.value(), typeName)

    private val stringValueTypeUrl: TypeUrl = TypeUrl.from(descriptor)

    @Nested internal inner class
    `create an instance by` {

        @Test
        fun `a message`() {
            val msg = TypeConverter.toMessage(Identifier.newUuid())
            val typeUrl = TypeUrl.of(msg)
            assertTypeUrl(typeUrl)
        }

        @Test
        fun `fully-qualified type name`() {
            val typeUrl = TypeName.of(typeName).toUrl()
            assertTypeUrl(typeUrl)
        }

        @Test
        fun `a type URL`() {
            val typeUrl = TypeUrl.parse(typeUrlValue)
            assertTypeUrl(typeUrl)
        }

        @Test
        fun `a descriptor of standard Protobuf type`() {
            val typeUrl = TypeUrl.from(descriptor)
            assertTypeUrl(typeUrl)
        }

        @Test
        fun `a descriptor of Spine type`() {
            val descriptor = EntityOption.getDescriptor()
            val expectedUrl = TypeUrl.composeTypeUrl(
                TypeUrl.Prefix.SPINE.value(),
                descriptor.fullName
            )

            val typeUrl = TypeUrl.from(descriptor)

            typeUrl.value() shouldBe expectedUrl
        }

        @Test
        fun `enum descriptor of Protobuf type`() {
            assertCreatedTypeUrl(
                TypeUrl.Prefix.GOOGLE_APIS.value(),
                Field.Kind.getDescriptor()
            )
        }

        @Test
        fun `enum descriptor of Spine type`() {
            assertCreatedTypeUrl(
                TypeUrl.Prefix.SPINE.value(),
                EntityOption.Kind.getDescriptor()
            )
        }

        @Test
        fun `by a class`() {
            val typeUrl = TypeUrl.of(StringValue::class.java)
            assertTypeUrl(typeUrl)
        }

        /**
         * Asserts properties that the passed type URL.
         *
         * Assertions are based on the assumption that the passed instance was created by
         * one of the inputs specified as test environment constants declared in this test suite.
         *
         * @see typeUrlValue
         * @see typeName
         * @see typeUrlValue
         */
        private fun assertTypeUrl(typeUrl: TypeUrl) {
            typeUrl.value() shouldBe typeUrlValue
            typeUrl.prefix() shouldBe TypeUrl.Prefix.GOOGLE_APIS.value()
            typeUrl.typeName().value() shouldBe typeName
            TypeName.from(typeUrl).simpleName() shouldBe StringValue::class.java.simpleName
        }

        private fun assertCreatedTypeUrl(
            expectedPrefix: String,
            descriptor: EnumDescriptor
        ) {
            val expected = TypeUrl.composeTypeUrl(expectedPrefix, descriptor.fullName)
            val typeUrl = TypeUrl.from(descriptor)
            typeUrl.value() shouldBe expected
        }
    }

    /**
     * This class tests cases when a type URL is defined without a prefix.
     *
     * At the time of writing, Protobuf does not specify how a type URL prefix is supposed to
     * be used. The only place it is used when [ packing a message][Any.pack] into `Any`.
     *
     * Even though Spine provides convenience features (i.e. in the form of custom Protobuf
     * file option `(type_url_prefix)` for working with type URLs, the framework does not
     * provide features for exposing type information via a site.
     *
     * Therefore, it is unreasonable to require using type URL prefixes until such a support
     * is provided by Protobuf, or by the Spine framework.
     */
    @Nested internal inner class
    `allow empty prefix when` {

        private lateinit var noPrefixType: TypeUrl

        @Test
        fun `parsing type name`() {
            noPrefixType = TypeUrl.parse("/package.Type")
            assertEmptyPrefix()
        }

        @Test
        fun `created for a type declared in a file with empty '(type_url_prefix)'`() {
            noPrefixType = TypeUrl.from(TypeWithoutPrefix.getDescriptor())
            assertEmptyPrefix()
        }

        private fun assertEmptyPrefix() {
            noPrefixType.prefix().shouldBeEmpty()
        }
    }

    @Nested internal inner class
    reject {

        @Test
        fun `null URL`() {
            assertNpe { TypeUrl.parse(TestValues.nullRef()) }
        }

        @Test
        fun `empty URL`() {
            assertIllegalArgument { TypeUrl.parse("") }
        }

        @Test
        fun `invalid URL of a packed message`() {
            val any = any {
                typeUrl = "invalid_type_url"
            }
            val exception = assertThrows<RuntimeException> {
                TypeUrl.ofEnclosed(any)
            }
            (exception.cause is InvalidProtocolBufferException) shouldBe true
        }

        @Test
        fun `value without prefix separator`() {
            assertIllegalArgument { TypeUrl.parse("prefix:Type") }
        }

        @Test
        fun `empty type name`() {
            assertIllegalArgument { TypeUrl.parse("type.prefix/") }
        }

        @Test
        fun `malformed type URL`() {
            assertIllegalArgument { TypeUrl.parse("prefix/prefix/type.Name") }
        }
    }

    @Test
    fun `obtain prefix`() {
        stringValueTypeUrl.prefix() shouldBe TypeUrl.Prefix.GOOGLE_APIS.value()
    }

    @Test
    fun `obtain type name`() {
        stringValueTypeUrl.typeName().value() shouldBe typeName
    }

    @Test
    fun `obtain a type name`() {
        stringValueTypeUrl.typeName() shouldBe TypeName.of(typeName)
    }

    @Test
    fun `throw 'UnknownTypeException' when trying to obtain unknown Java class`() {
        val url = TypeUrl.parse("unknown/JavaClass")
        assertUnknownType { url.toJavaClass() }
    }

    @Nested inner class
    `create an instance via 'GenericDescriptor'` {

        @Test
        fun `of message`() {
            TypeUrl.ofTypeOrService(Any.getDescriptor()) shouldBe
                    TypeUrl.of(Any.getDefaultInstance())
        }

        @Test
        fun `of enum`() {
            val descr = Uri.Schema.getDescriptor()
            TypeUrl.ofTypeOrService(descr) shouldBe TypeUrl.from(descr)
        }

        @Test
        fun `of service`() {
            val descr = GreetingService.getDescriptor()
            TypeUrl.ofTypeOrService(descr) shouldBe TypeUrl.from(descr)
        }

        @Test
        fun `rejecting other types of descriptors`() {
            assertRejects(AnyProto.getDescriptor()) // File descriptor.
            assertRejects(Any.getDescriptor().field(1)!!) // Field descriptor.
            assertRejects(Transmission.getDescriptor().realOneofs[0]) // Oneof descriptor.
        }

        private fun assertRejects(d: GenericDescriptor) {
            assertThrows<IllegalArgumentException> {
                TypeUrl.ofTypeOrService(d)
            }
        }
    }

    @Test
    fun `provide equality`() {
        EqualsTester()
            .addEqualityGroup(
                TypeUrl.of(StringValue::class.java),
                TypeUrl.of(StringValue::class.java)
            )
            .addEqualityGroup(TypeUrl.of(Timestamp::class.java))
            .addEqualityGroup(TypeUrl.of(UInt32Value::class.java))
            .testEquals()
    }

    @Test
    fun `have string representation`() {
        TypeUrl.of(BoolValue::class.java).toString() shouldContain BoolValue::class.java.simpleName
    }

    @Test
    fun `have popular type prefixes`() {
        TypeUrl.Prefix.SPINE.toString() shouldBe "type.spine.io"
        TypeUrl.Prefix.GOOGLE_APIS.toString() shouldBe "type.googleapis.com"
    }

    @Test
    fun `be serializable`() {
        SerializableTester.reserializeAndAssert(TypeUrl.of(Timestamp::class.java))
    }
}
