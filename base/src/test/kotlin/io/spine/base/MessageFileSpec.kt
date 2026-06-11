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
