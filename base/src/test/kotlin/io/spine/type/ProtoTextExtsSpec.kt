/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.type

import com.google.protobuf.Timestamp
import com.google.protobuf.stringValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.spine.base.MapOfAnys
import io.spine.base.MapOfAnysKt.entry
import io.spine.base.Time
import io.spine.base.mapOfAnys
import io.spine.protobuf.pack
import io.spine.string.Indent
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Kotlin extensions for proto text output should")
internal class ProtoTextExtsSpec {

    @Nested inner class
    `print short debug string of` {

        private lateinit var msg: Timestamp
        private lateinit var debugStr: String

        @BeforeEach
        fun createMessage() {
            msg = Time.currentTime()
            if (msg.nanos == 0) {
                msg = msg.toBuilder().setNanos(1).build()
            }
        }

        private fun assertData() {
            debugStr shouldContain "seconds"
            debugStr shouldContain "nanos"
        }

        @Test
        fun `'Message'`() {
            debugStr = msg.shortDebugString()
            assertData()
        }

        @Test
        fun `message 'Builder'`() {
            debugStr = msg.toBuilder().shortDebugString()
            assertData()
        }

        @Test
        fun `'Any' with the packed message data`() {
            debugStr = msg.pack().shortDebugString()
            assertData()
            debugStr shouldContain "[type.googleapis.com/google.protobuf.Timestamp]"
        }
    }

    @Nested inner class
    `print proto text output with name which` {

        private lateinit var msg: MapOfAnys
        private lateinit var textOut: String
        private lateinit var lines: List<String>

        @BeforeEach
        fun createMessage() {
            msg = mapOfAnys {
                entry.add(entry {
                    key = Time.currentTime().pack()
                    value = stringValue { UUID.randomUUID().toString() }.pack()
                })
            }
            textOut = msg.printToStringWithName()
            lines = textOut.lines()
        }

        @Test
        fun `starts with the type name`() {
            textOut shouldStartWith msg.descriptorForType.fullName
        }

        @Test
        fun `have type name ended with curly brace`() {
            lines[0] shouldEndWith " {"
        }

        @Test
        fun `is indented in the fields block`() {
            val expectedIndent = Indent.defaultProtoTextIndent.value

            lines[1] shouldStartWith expectedIndent
            lines[2] shouldStartWith expectedIndent

            lines[1][expectedIndent.length] shouldNotBe " "
            lines[2][expectedIndent.length] shouldNotBe " "
        }

        @Test
        fun `close with curly brace`() {
            lines[lines.size - 2] shouldBe "}"
            lines.last() shouldBe ""
        }
    }
}
