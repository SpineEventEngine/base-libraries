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

import com.google.protobuf.Message
import com.google.protobuf.stringValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.spine.test.protobuf.MessageToPack
import io.spine.test.protobuf.messageToPack
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


@DisplayName("`Any` Kotlin extensions should")
class AnyExtensionsSpec {

    @Test
    fun `unpack Any into a concrete type`() {
        val msg = messageToPack {
            value = stringValue { value = "bla bla" }
        }
        val any = AnyPacker.pack(msg)

        val unpacked = any.unpack<MessageToPack>()

        unpacked shouldBe msg
    }

    @Test
    fun `unpack Any without a concrete type`() {
        val msg = messageToPack {
            value = stringValue { value = "foo bar" }
        }
        val any = AnyPacker.pack(msg)

        val unpacked = any.unpackKnownType()

        unpacked shouldBe instanceOf<MessageToPack>()
        unpacked shouldBe msg
    }

    @Test
    fun `fail to unpack Any with an interface`() {
        val msg = messageToPack {
            value = stringValue { value = "la la la" }
        }
        val any = AnyPacker.pack(msg)

        assertThrows<IllegalArgumentException> {
            any.unpack<Message>()
        }
    }

    @Test
    fun `pack message into Any`() {
        val msg = messageToPack {
            value = stringValue { value = "pack in fun" }
        }
        val any = msg.pack()

        any.typeUrl shouldBe "type.spine.io/spine.test.protobuf.MessageToPack"
        any.unpack<MessageToPack>() shouldBe msg
    }
}
