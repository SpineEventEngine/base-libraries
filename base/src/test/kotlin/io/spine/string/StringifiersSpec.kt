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
package io.spine.string

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import com.google.protobuf.util.Durations
import com.google.protobuf.util.Timestamps
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.base.Identifier
import io.spine.base.Time
import io.spine.string.Stringifiers.stringify
import io.spine.test.string.STask
import io.spine.test.string.STaskId
import io.spine.test.string.sTask
import io.spine.test.string.sTaskId
import io.spine.test.string.STaskStatus
import io.spine.testing.UtilityClassTest
import io.spine.type.toCompactJson
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress(
    "TestFunctionName" /* Allow starting with a capital letter when they are named after a type. */
)
@DisplayName("`Stringifiers` utility class should")
internal class StringifiersSpec : UtilityClassTest<Stringifiers>(Stringifiers::class.java) {

    companion object {
        private const val DELIMITER = '#'
        private const val SIZE = 5
    }

    @Nested internal inner class
    `stringify` {

        @Test
        fun boolean() = checkStringifies(false, "false")

        @Test
        fun int() = checkStringifies(1, "1")

        @Test
        fun long() = checkStringifies(1L, "1")

        @Test
        fun String() {
            val theString = "some-string"
            checkStringifies(theString, theString)
        }

        @Test
        fun Timestamp() {
            val timestamp = Timestamp.getDefaultInstance()
            val expected = Timestamps.toString(timestamp)
            checkStringifies(timestamp, expected)
        }

        @Test
        fun Duration() {
            val duration = Duration.getDefaultInstance()
            val expected = Durations.toString(duration)
            checkStringifies(duration, expected)
        }

        @Test
        fun enums() {
            checkStringifies(STaskStatus.DONE, "DONE")
        }

        @Test
        fun `a Protobuf 'Message'`() {
            val id = sTaskId {
                uuid = Identifier.newUuid()
            }
            val message = sTask {
                this.id = id
                status = STaskStatus.DONE
            }

            val expected = message.toCompactJson()
            checkStringifies(message, expected)
        }
        private fun checkStringifies(value: Any, expected: String) {
            val conversionResult = Stringifiers.toString(value)
            conversionResult shouldBe expected
        }
    }

    @Nested internal inner class
    `create 'Stringifier' with a delimeter for` {

        @Test
        fun List() {
            val stamps: List<Timestamp> = createList()
            val stringifier = listStringifier<Timestamp>(DELIMITER)
            val out = stringifier.toString(stamps)

            out shouldContain DELIMITER.toString()

            val quoter = Quoter.forLists()
            for (stamp in stamps) {
                out shouldContain quoter.quote(Timestamps.toString(stamp))
            }
        }

        @Test
        fun Map() {
            val stamps = createMap()
            val stringifier = mapStringifier<Long, Timestamp>(DELIMITER)
            val out = stringifier.toString(stamps)

            out shouldContain DELIMITER.toString()

            val quoter = Quoter.forMaps()
            for (key in stamps.keys) {
                out shouldContain key.toString()
                out shouldContain quoter.quote(Timestamps.toString(stamps[key]))
            }
        }

        private fun createList(): ImmutableList<Timestamp> {
            val builder = ImmutableList.builder<Timestamp>()
            for (i in 0..<SIZE) {
                builder.add(Time.currentTime())
            }
            return builder.build()
        }

        private fun createMap(): ImmutableMap<Long, Timestamp> {
            val builder = ImmutableMap.builder<Long, Timestamp>()
            for (i in 0..<SIZE) {
                val t = Time.currentTime()
                builder.put(i.toLong(), t)
            }
            return builder.build()
        }
    }

    /**
     * This class covers only cases that are not touched by other tests that
     * involve parsing of string values.
     */
    @Nested internal inner class
    `parse a string into` {

        @Test
        fun Boolean() {
            fromString<Boolean>("true") shouldBe true
        }

        @Test
        fun Integer() {
            fromString<Int>("-100500") shouldBe -100500
        }

        @Test
        fun List() {
            val numbers: List<Int> = ImmutableList.of(100, 200, -300)
            val stringifier = listStringifier<Int>()
            val numString = stringifier.toString(numbers)

            val parsed = stringifier.fromString(numString)

            parsed shouldContainExactly numbers
        }
    }

    @Test
    fun `have an alias for 'toString' method`() {
        val value = "foo-bar"
        Stringifiers.toString(value) shouldBe stringify(value)
    }
}
