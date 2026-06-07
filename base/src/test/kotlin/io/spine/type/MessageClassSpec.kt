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

import com.google.common.testing.EqualsTester
import com.google.common.testing.SerializableTester.reserializeAndAssert
import com.google.protobuf.Any
import com.google.protobuf.Message
import com.google.protobuf.StringValue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`MessageClass` should")
internal class MessageClassSpec {

    @Test
    fun `provide equality within the class`() {
        EqualsTester()
            .addEqualityGroup(
                TestMessageClass(MSG_CLASS),
                TestMessageClass(MSG_CLASS),
                TestMessageClass(MSG_CLASS, MSG_TYPE)
            )
            .addEqualityGroup(
                TestMessageClass(MSG_CLASS, OTHER_TYPE),
                TestMessageClass(MSG_CLASS, OTHER_TYPE)
            )
            .testEquals()
    }

    @Test
    fun `serialize and deserialize`() {
        reserializeAndAssert(TestMessageClass(MSG_CLASS))
    }

    @Test
    fun `obtain the name of the type`() {
        TestMessageClass(MSG_CLASS).typeName() shouldBe MSG_TYPE.typeName()
    }

    /**
     * A cursory test obtaining the interfaces extending [Message].
     *
     * Since this module has no code generation that would produce interfaces
     * extending [Message], the test is limited and simply makes use of the method.
     */
    @Test
    fun `obtain super interfaces of a class`() {
        MessageClass.interfacesOf(HierarchyStub::class.java)
            .shouldContainExactlyInAnyOrder(
                SubSubMessage::class.java,
                SubMessage::class.java,
                SuperMessage::class.java
            )
    }

    @Test
    fun `obtain the type URL`() {
        TestMessageClass(MSG_CLASS, MSG_TYPE).typeUrl() shouldBe MSG_TYPE
    }

    @Test
    fun `not be equal to an instance of a different type`() {
        @Suppress("EqualsBetweenInconvertibleTypes")
        val equalToString = TestMessageClass(MSG_CLASS).equals("not a message class")
        equalToString shouldBe false
    }

    @Test
    fun `not be equal to a message class wrapping another Java class`() {
        TestMessageClass(StringValue::class.java) shouldNotBe TestMessageClass(Any::class.java)
    }

    private companion object {
        val MSG_CLASS: Class<StringValue> = StringValue::class.java
        val MSG_TYPE: TypeUrl = TypeUrl.of(StringValue::class.java)
        val OTHER_TYPE: TypeUrl = TypeUrl.of(Any::class.java)
    }
}

/**
 * A test environment class extending the abstract [MessageClass].
 *
 * Declared as a top-level class to avoid capturing an outer instance during
 * the serialization round-trip.
 */
private class TestMessageClass : MessageClass<Message> {

    constructor(value: Class<out Message>) : super(value)

    constructor(value: Class<out Message>, url: TypeUrl) : super(value, url)

    private companion object {
        @Suppress("ConstPropertyName", "unused")
        private const val serialVersionUID: Long = 0L
    }
}

private interface SuperMessage : Message

private interface SubMessage : SuperMessage

private interface SubSubMessage : SubMessage

/**
 * A stub type used only via reflection in the interfaces test; never instantiated.
 */
private abstract class HierarchyStub : SubSubMessage
