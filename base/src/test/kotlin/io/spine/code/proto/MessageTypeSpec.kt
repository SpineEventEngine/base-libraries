/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.google.common.truth.IterableSubject
import com.google.common.truth.Truth.assertThat
import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Timestamp
import com.google.protobuf.value
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.option.EntityOption
import io.spine.option.GoesOption
import io.spine.option.MinOption
import io.spine.test.code.proto.command.MttStartProject
import io.spine.test.code.proto.event.MttProjectStarted
import io.spine.test.code.proto.rejections.TestRejections
import io.spine.test.code.proto.uuid.MttEntityState
import io.spine.test.code.proto.uuid.MttUuidMessage
import io.spine.test.type.PersonName
import io.spine.test.type.Uri
import io.spine.test.type.Url
import io.spine.type.EnumType
import io.spine.type.MessageType
import java.util.function.Predicate
import java.util.function.Predicate.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`MessageType` should")
internal class MessageTypeSpec {

    @Nested internal inner class
    `tell if a type` {

        /**
         * Tests a certain boolean method of `MessageType` created on the passed descriptor.
         */
        infix fun Descriptor.shouldPass(predicate: Predicate<MessageType>) {
            val type = MessageType(this)
            val result = predicate.test(type)
            result shouldBe true
        }

        @Nested internal inner class
        `be` {

            @Test
            fun nested() {
                Uri.Protocol.getDescriptor() shouldPass { it.isNested }
                Url.getDescriptor() shouldPass not { it.isNested }
            }

            @Test
            fun `top level`() {
                Url.getDescriptor() shouldPass { it.isTopLevel }
                Uri.Protocol.getDescriptor()  shouldPass not { it.isTopLevel }
            }

            @Test
            fun `a rejection`() {
                TestRejections.MttSampleRejection.getDescriptor() shouldPass { it.isRejection }
            }

            @Test
            fun `a command`() {
                MttStartProject.getDescriptor() shouldPass { it.isCommand }
            }

            @Test
            fun `an event`() {
                MttProjectStarted.getDescriptor() shouldPass { it.isEvent }
            }

            @Test
            fun `a UUID value`() {
                MttUuidMessage.getDescriptor() shouldPass { it.isUuidValue }
            }

            @Test
            fun `an entity state`() {
                MttEntityState.getDescriptor() shouldPass { it.isEntityState }
            }

            /**
             * This test suite takes nested types of corresponding signals to
             * verify that they are not seen as signals of the kind of the enclosing types.
             */
            @Nested internal inner class
            `not` {

                @Test
                fun `a rejection`() {
                    TestRejections.MttSampleRejection.Details.getDescriptor() shouldPass
                        not { it.isRejection }
                }

                @Test
                fun `a command`() {
                    MttStartProject.Details.getDescriptor() shouldPass not { it.isCommand }
                }

                @Test
                fun `an event`() {
                    MttProjectStarted.Details.getDescriptor() shouldPass not { it.isEvent }
                }

                @Test
                fun `a UUID value`() {
                    MttProjectStarted.getDescriptor() shouldPass not { it.isUuidValue }
                }

                @Test
                fun `an entity state`() {
                    MttStartProject.getDescriptor() shouldPass not { it.isEntityState }
                }
            }

            @Nested internal inner class
            `a non-Google or a Spine options type` {

                @Test
                fun `positively for a custom type`() {
                    Url.getDescriptor() shouldPass { it.isCustom }
                }

                @Test
                fun `negatively for Google type`() {
                    Timestamp.getDescriptor() shouldPass not { it.isCustom }
                }

                @Test
                fun `negatively for Spine options type`() {
                    GoesOption.getDescriptor() shouldPass not { it.isCustom }
                    EntityOption.getDescriptor() shouldPass not { it.isCustom }
                    MinOption.getDescriptor() shouldPass not { it.isCustom }
                }
            }
        }
    }

    @Nested internal inner class
    `obtain a path for` {

        @CanIgnoreReturnValue
        private fun assertPath(descriptor: Descriptor): IterableSubject {
            val type = MessageType(descriptor)
            val path = type.path()
            val assertPath = assertThat(path.toList())
            assertPath.contains(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER)
            assertPath.contains(descriptor.index)
            return assertPath
        }

        @Test
        fun `top-level message`() {
            assertPath(Url.getDescriptor())
        }

        @Test
        fun `second-level message`() {
            val assertPath = assertPath(Uri.Protocol.getDescriptor())
            assertPath.contains(Uri.getDescriptor().index)
        }
    }

    @Test
    fun `obtain nested type declarations`() {
        val uriType = MessageType.of(Uri.getDefaultInstance())
        val nestedType = uriType.nestedDeclarations()

        nestedType should containExactly(
            MessageType(Uri.Protocol.getDescriptor()),
            MessageType(Uri.Authorization.getDescriptor()),
            MessageType(Uri.QueryParameter.getDescriptor()),
            EnumType.create(Uri.Schema.getDescriptor())
        )
    }

    @Test
    fun `obtain type description`() {
        val uriType = MessageType(Uri.getDescriptor())
        val description = uriType.leadingComments()

        description shouldBePresent {
            it shouldContain "A URL in a structured form."
        }
    }

    @Test
    fun `tell if an option is applied to the type`() {
        MessageType(PersonName.getDescriptor()).hasOption("required_field") shouldBe true
    }

    @Test
    fun `tell it supports builders`() {
        MessageType(Uri.getDescriptor()).supportsBuilders() shouldBe true
    }

    @Test
    fun `support equality and hashing`() {
        EqualsTester()
            .addEqualityGroup(type(Url.getDescriptor()), type(Url.getDescriptor()))
            .addEqualityGroup(type(Timestamp.getDescriptor()))
            .testEquals()
    }

    companion object {
        private fun type(descriptor: Descriptor): MessageType {
            return MessageType(descriptor)
        }
    }
}
