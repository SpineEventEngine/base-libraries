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

package io.spine.base

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.OneofDescriptor
import com.google.protobuf.Message
import com.google.protobuf.Parser
import com.google.protobuf.UnknownFieldSet
import io.kotest.matchers.shouldBe
import java.io.OutputStream
import java.io.Serial
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class `Extensions for 'Throwable' should` {

    @Nested inner class
    `tell if it was caused by a rejection` {

        private var throwable: Throwable = RuntimeException()

        @BeforeEach
        fun createThrowable() {
            throwable = RuntimeException()
        }

        @Test
        fun `if true`() {
            throwable.initCause(StubRejectionThrowable())
            throwable.causedByRejection() shouldBe true
        }

        @Test
        fun `if false`() {
            throwable.initCause(RuntimeException())
            throwable.causedByRejection() shouldBe false
        }
    }

    /**
     * Stub type of [RejectionThrowable] that serves only as a type.
     */
    private class StubRejectionThrowable: RejectionThrowable(StubRejectionMessage()) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = 0L
        }
    }

    /**
     * Stub type of [RejectionMessage] that serves only as a type.
     */
    @Suppress("TooManyFunctions")
    private class StubRejectionMessage: RejectionMessage {
        override fun getDefaultInstanceForType(): Message = notImplemented()
        override fun isInitialized(): Boolean = notImplemented()
        override fun writeTo(output: CodedOutputStream?): Unit = notImplemented()
        override fun writeTo(output: OutputStream?): Unit = notImplemented()
        override fun getSerializedSize(): Int = notImplemented()
        override fun getParserForType(): Parser<out Message> = notImplemented()
        override fun toByteString(): ByteString = notImplemented()
        override fun toByteArray(): ByteArray = notImplemented()
        override fun writeDelimitedTo(output: OutputStream?): Unit = notImplemented()
        override fun newBuilderForType(): Message.Builder = notImplemented()
        override fun toBuilder(): Message.Builder = notImplemented()
        override fun findInitializationErrors(): MutableList<String> = notImplemented()
        override fun getInitializationErrorString(): String = notImplemented()
        override fun getDescriptorForType(): Descriptor = notImplemented()
        override fun getAllFields(): MutableMap<FieldDescriptor, Any> = notImplemented()
        override fun hasOneof(oneof: OneofDescriptor?): Boolean = notImplemented()
        override fun getOneofFieldDescriptor(oneof: OneofDescriptor?): FieldDescriptor =
            notImplemented()
        override fun hasField(field: FieldDescriptor?): Boolean = notImplemented()
        override fun getField(field: FieldDescriptor?): Any = notImplemented()
        override fun getRepeatedFieldCount(field: FieldDescriptor?): Int = notImplemented()
        override fun getRepeatedField(field: FieldDescriptor?, index: Int): Any = notImplemented()
        override fun getUnknownFields(): UnknownFieldSet = notImplemented()
        private fun notImplemented(): Nothing = TODO("Stub type")

        companion object {
            @Serial
            private const val serialVersionUID: Long = 0L
        }
    }
}
