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
