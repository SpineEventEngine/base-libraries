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

package io.spine.code.proto

import com.google.common.testing.EqualsTester
import com.google.common.truth.IterableSubject
import com.google.common.truth.Truth.assertThat
import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Timestamp
import com.google.protobuf.value
import io.kotest.assertions.throwables.shouldThrow
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

    @Test
    fun `convert itself to a descriptor proto`() {
        val descriptor = Url.getDescriptor()
        type(descriptor).toProto() shouldBe descriptor.toProto()
    }

    @Nested inner class
    `tell if it is a signal` {

        @Test
        fun `positively for a command`() {
            type(MttStartProject.getDescriptor()).isSignal shouldBe true
        }

        @Test
        fun `positively for an event`() {
            type(MttProjectStarted.getDescriptor()).isSignal shouldBe true
        }

        @Test
        fun `positively for a rejection`() {
            type(TestRejections.MttSampleRejection.getDescriptor()).isSignal shouldBe true
        }

        @Test
        fun `negatively for a non-signal`() {
            type(MttUuidMessage.getDescriptor()).isSignal shouldBe false
        }
    }

    @Nested inner class
    `obtain a field declaration` {

        @Test
        fun `by its name`() {
            val field = type(Uri.getDescriptor()).field("host")
            field.name().value() shouldBe "host"
        }

        @Test
        fun `rejecting an unknown name`() {
            shouldThrow<IllegalArgumentException> {
                type(Uri.getDescriptor()).field("no_such_field")
            }
        }
    }

    @Test
    fun `not be a UUID value when it has more than one field`() {
        type(Timestamp.getDescriptor()).isUuidValue shouldBe false
    }

    companion object {
        private fun type(descriptor: Descriptor): MessageType {
            return MessageType(descriptor)
        }
    }
}
