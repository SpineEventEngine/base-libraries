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

import com.google.protobuf.StringValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.spine.given.type.ImplicitInternalType
import io.spine.test.type.Url
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`type` package members should")
internal class TypeMembersSpec {

    @Nested inner class
    `reject malformed type URLs` {

        @Test
        fun `a string without a separator`() {
            shouldThrow<IllegalArgumentException> {
                TypeUrl.parse("no-separator-here")
            }
        }

        @Test
        fun `a string with too many parts`() {
            shouldThrow<IllegalArgumentException> {
                TypeUrl.parse("prefix/type/extra")
            }
        }
    }

    @Test
    @DisplayName("create `UnknownTypeException` from a cause")
    fun unknownTypeFromCause() {
        val cause = RuntimeException("boom")
        val exception = UnknownTypeException(cause)
        exception.cause shouldBe cause
    }

    @Nested inner class
    `expose Type members via MessageType` {

        private val type = MessageType(Url.getDescriptor())

        @Test
        fun `Java package`() {
            type.javaPackage().value() shouldNotBe ""
        }

        @Test
        fun `simple Java class name`() {
            type.simpleJavaClassName().value() shouldBe "Url"
        }
    }

    @Nested inner class
    `verify a message class is published` {

        @Test
        fun `returning a published class`() {
            requirePublished(StringValue::class.java) shouldBe StringValue::class.java
        }

        @Test
        fun `rejecting an internal class`() {
            shouldThrow<UnpublishedLanguageException> {
                requirePublished(ImplicitInternalType::class.java)
            }
        }
    }

    @Nested inner class
    `describe a ServiceType` {

        private val descriptor =
            ImplicitInternalType.getDescriptor().file.services[0]

        @Test
        fun `as a proto`() {
            ServiceType.of(descriptor).toProto().name shouldBe descriptor.name
        }

        @Test
        fun `with a Java class name`() {
            ServiceType.of(descriptor).javaClassName().value() shouldContain descriptor.name
        }
    }
}
