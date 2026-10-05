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

package io.spine.protobuf

import com.google.protobuf.ByteString
import com.google.protobuf.DynamicMessage
import com.google.protobuf.Empty
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.any
import com.google.protobuf.stringValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.spine.base.Identifier.newUuid
import io.spine.base.Time
import io.spine.test.protobuf.MessageToPack
import io.spine.test.protobuf.messageToPack
import io.spine.test.type.TypeWithoutPrefix
import io.spine.test.type.typeWithoutPrefix
import io.spine.type.TypeUrl
import io.spine.type.UnexpectedTypeException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.google.protobuf.Any as AnyProto

@DisplayName("`AnyPacker` should")
internal class AnyPackerSpec {

    /**
     * A message of the type that declares the `type.spine.io` type URL prefix.
     */
    private val message = messageToPack {
        value = stringValue { value = newUuid() }
    }

    private val messageClass = MessageToPack::class.java

    /**
     * Packs [message] into an `Any` with a type URL that has several slashes.
     */
    private fun packWithSeveralSlashes(): AnyProto = any {
        typeUrl = "example.org/types/${message.descriptorForType.fullName}"
        value = message.toByteString()
    }

    @Test
    fun `return the same 'Any' from 'pack()'`() {
        val any = TypeConverter.toAny(javaClass.simpleName)

        AnyPacker.pack(any) shouldBeSameInstanceAs any
    }

    @Test
    fun `pack to 'Any'`() {
        val timestamp = Time.currentTime()

        AnyPacker.unpack(AnyPacker.pack(timestamp)) shouldBe timestamp
    }

    @Test
    fun `unpack disregarding the prefix of the type URL`() {
        // Protobuf packs with the `type.googleapis.com` prefix,
        // which differs from the one declared for the type.
        val typeUrl = AnyProto.pack(message).typeUrl
        typeUrl shouldNotBe TypeUrl.of(message).value()

        AnyPacker.unpack(AnyProto.pack(message), messageClass) shouldBe message
        AnyPacker.unpack(AnyProto.pack(message)) shouldBe message
    }

    @Test
    fun `accept a type URL with several slashes`() {
        // The type name is the content after the last slash.
        AnyPacker.unpack(packWithSeveralSlashes(), messageClass) shouldBe message
        AnyPacker.unpack(packWithSeveralSlashes()) shouldBe message
    }

    @Test
    fun `unpack a message of a type declared without a type URL prefix`() {
        val noPrefixMessage = typeWithoutPrefix { value = newUuid() }
        val any = AnyPacker.pack(noPrefixMessage)
        any.typeUrl shouldStartWith "/"

        AnyPacker.unpack(any, TypeWithoutPrefix::class.java) shouldBe noPrefixMessage
    }

    @Nested inner class
    `remember the unpacked message and` {

        @Test
        fun `return it when the same 'Any' is unpacked again`() {
            val any = AnyPacker.pack(message)

            val first = AnyPacker.unpack(any, messageClass)
            val second = AnyPacker.unpack(any, messageClass)

            first shouldBe message
            second shouldBeSameInstanceAs first
        }

        @Test
        fun `return it from every unpacking method`() {
            val any = AnyPacker.pack(message)

            val unpacked = AnyPacker.unpack(any, messageClass)

            AnyPacker.unpack(any) shouldBeSameInstanceAs unpacked
            AnyPacker.unpackFunc().apply(any) shouldBeSameInstanceAs unpacked
            AnyPacker.unpackFunc(messageClass).apply(any) shouldBeSameInstanceAs unpacked
        }

        @Test
        fun `parse an equal but distinct 'Any' anew`() {
            val any = AnyPacker.pack(message)
            val unpacked = AnyPacker.unpack(any, messageClass)

            // The remembered message is not a part of the value of `Any`.
            val equalAny = AnyPacker.pack(message)
            equalAny shouldBe any
            equalAny shouldNotBeSameInstanceAs any

            val unpackedAnew = AnyPacker.unpack(equalAny, messageClass)

            unpackedAnew shouldBe unpacked
            unpackedAnew shouldNotBeSameInstanceAs unpacked
        }
    }

    @Nested inner class
    `throw 'UnexpectedTypeException' if` {

        @Test
        fun `the type of the packed message does not match the class`() {
            val any = AnyPacker.pack(message)

            shouldThrow<UnexpectedTypeException> {
                AnyPacker.unpack(any, Empty::class.java)
            }
        }

        @Test
        fun `the type does not match the class after the 'Any' was unpacked`() {
            val any = AnyPacker.pack(message)
            AnyPacker.unpack(any, messageClass)

            shouldThrow<UnexpectedTypeException> {
                AnyPacker.unpack(any, Empty::class.java)
            }
        }

        @Test
        fun `the packed bytes cannot be parsed`() {
            val malformed = any {
                typeUrl = TypeUrl.of(Empty::class.java).value()
                value = ByteString.copyFromUtf8("malformed bytes")
            }
            // The type matches. So, the failure must come from parsing the bytes.
            malformed.isSameTypeAs(Empty.getDefaultInstance()) shouldBe true

            val exception = shouldThrow<UnexpectedTypeException> {
                AnyPacker.unpack(malformed, Empty::class.java)
            }
            exception.cause.shouldBeInstanceOf<InvalidProtocolBufferException>()
        }

        @Test
        fun `the type URL is empty`() {
            val noTypeUrl = any {
                value = message.toByteString()
            }

            shouldThrow<UnexpectedTypeException> {
                AnyPacker.unpack(noTypeUrl, messageClass)
            }
        }

        @Test
        fun `the 'Any' remembers a message of another Java class`() {
            val any = AnyPacker.pack(message)
            // Make the `Any` remember the message as `DynamicMessage`,
            // as code using Protobuf directly could do.
            val exemplar = DynamicMessage.getDefaultInstance(MessageToPack.getDescriptor())
            any.unpackSameTypeAs(exemplar)

            shouldThrow<UnexpectedTypeException> {
                AnyPacker.unpack(any, messageClass)
            }
        }
    }
}
