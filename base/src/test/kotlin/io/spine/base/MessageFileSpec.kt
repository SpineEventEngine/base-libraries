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

import com.google.protobuf.Any
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`MessageFile` should")
internal class MessageFileSpec {

    @Test
    fun `expose the suffix required for the corresponding kind of files`() {
        MessageFile.COMMANDS.suffix() shouldBe "commands.proto"
        MessageFile.EVENTS.suffix() shouldBe "events.proto"
        MessageFile.REJECTIONS.suffix() shouldBe "rejections.proto"
    }

    @Nested internal inner class
    `test a file descriptor` {

        @Test
        fun `accepting the file with matching suffix`() {
            val file = FileDescriptorProto.newBuilder()
                .setName("given_events.proto")
                .build()
            MessageFile.EVENTS.test(file) shouldBe true
        }

        @Test
        fun `rejecting the file with non-matching suffix`() {
            val file = Any.getDescriptor().file.toProto()
            MessageFile.EVENTS.test(file) shouldBe false
        }
    }
}
