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

package io.spine.code.proto

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Timestamp
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`FileDescriptorSetReader` should")
internal class FileDescriptorSetReaderSpec {

    /** Bytes of a valid, non-empty [FileDescriptorSet]. */
    private val validBytes: ByteArray = FileDescriptorSet.newBuilder()
        .addFile(Timestamp.getDescriptor().file.toProto())
        .build()
        .toByteArray()

    /**
     * Bytes that cannot be parsed as a [FileDescriptorSet]: a length-delimited
     * field declares five bytes of content, but only one follows.
     */
    private val malformedBytes: ByteArray = byteArrayOf(0x0A, 0x05, 0x01)

    @Nested inner class
    `parse a byte array` {

        @Test
        fun `into a descriptor set`() {
            val parsed = FileDescriptorSetReader.parse(validBytes)
            parsed.fileCount shouldBe 1
        }

        @Test
        fun `throwing 'IllegalArgumentException' on malformed input`() {
            shouldThrow<IllegalArgumentException> {
                FileDescriptorSetReader.parse(malformedBytes)
            }
        }
    }

    @Nested inner class
    `attempt to parse a byte array` {

        @Test
        fun `returning the descriptor set when valid`() {
            val parsed = FileDescriptorSetReader.tryParse(validBytes)
            parsed.shouldBePresent()
            parsed.get().fileCount shouldBe 1
        }

        @Test
        fun `returning an empty 'Optional' on malformed input`() {
            FileDescriptorSetReader.tryParse(malformedBytes).shouldBeEmpty()
        }
    }

    @Nested inner class
    `parse a stream` {

        @Test
        fun `into a descriptor set`() {
            val parsed = FileDescriptorSetReader.parse(ByteArrayInputStream(validBytes))
            parsed.fileCount shouldBe 1
        }

        @Test
        fun `throwing 'IllegalArgumentException' on malformed input`() {
            shouldThrow<IllegalArgumentException> {
                FileDescriptorSetReader.parse(ByteArrayInputStream(malformedBytes))
            }
        }
    }
}
