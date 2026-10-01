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
