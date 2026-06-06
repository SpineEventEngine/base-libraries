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

package io.spine.type

import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.google.protobuf.StringValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.base.Identifier
import io.spine.json.given.Node
import io.spine.json.given.node
import io.spine.json.given.WrappedString
import io.spine.testing.Assertions.assertNpe
import io.spine.testing.TestValues
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Json` Kotlin extensions for Protobuf should")
internal class JsonSpec {

    @Test
    fun `not allow null message`() {
        assertNpe { TestValues.nullRef<Message>().toJson() }
    }

    @Test
    fun `print to JSON`() {
        val value = StringValue.of("print_to_json")
        value.toJson() shouldNotBe ""
    }

    @Test
    fun `print to compact JSON`() {
        val idValue = Identifier.newUuid()
        val node = node {
            name = idValue
            right = Node.getDefaultInstance()
        }
        val result = node.toCompactJson()

        result shouldNotBe ""
        result.contains(System.lineSeparator()) shouldBe false
    }

    @Test
    fun `parse from JSON`() {
        val idValue = Identifier.newUuid()
        val jsonMessage = String.format(Locale.ROOT, "{\"value\": \"%s\"}", idValue)
        val parsedValue = WrappedString::class.java.fromJson(jsonMessage)

        parsedValue shouldNotBe null
        parsedValue.value shouldBe idValue
    }

    @Test
    fun `parse from JSON with unknown values`() {
        val idValue = Identifier.newUuid()
        val jsonMessage =
            String.format(Locale.ROOT, "{\"value\": \"%s\", \"newField\": \"newValue\"}", idValue)
        val parsedValue = WrappedString::class.java.fromJson(jsonMessage)

        parsedValue shouldNotBe null
        parsedValue.value shouldBe idValue
    }

    @Test
    fun `throw 'IllegalStateException' when parsing malformed JSON`() {
        shouldThrow<IllegalStateException> {
            WrappedString::class.java.fromJson("{ this is not valid JSON")
        }
    }

    @Test
    fun `throw 'UnknownTypeException' when printing a message of an unknown type`() {
        val unknownTypeMessage = Any.newBuilder()
            .setTypeUrl("type.googleapis.com/non.existent.UnknownType")
            .setValue(ByteString.copyFromUtf8("data"))
            .build()

        shouldThrow<UnknownTypeException> {
            unknownTypeMessage.toJson()
        }
    }
}
