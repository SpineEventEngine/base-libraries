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

package io.spine.protobuf

import com.google.common.testing.NullPointerTester
import com.google.protobuf.Duration
import com.google.protobuf.util.Durations
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.spine.protobuf.Durations2.add
import io.spine.protobuf.Durations2.hours
import io.spine.protobuf.Durations2.hoursAndMinutes
import io.spine.protobuf.Durations2.isGreaterThan
import io.spine.protobuf.Durations2.isLessThan
import io.spine.protobuf.Durations2.isPositiveOrZero
import io.spine.protobuf.Durations2.isZero
import io.spine.protobuf.Durations2.milliseconds
import io.spine.protobuf.Durations2.minutes
import io.spine.protobuf.Durations2.nanos
import io.spine.protobuf.Durations2.seconds
import io.spine.protobuf.Durations2.toJavaTime
import io.spine.testing.TestValues
import io.spine.testing.UtilityClassTest
import io.spine.testing.setDefault
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`Durations2` utility class should")
internal class Durations2Spec : UtilityClassTest<Durations2>(Durations2::class.java) {

    private val converter = Durations2.converter()

    override fun configure(nullTester: NullPointerTester) {
        nullTester.setDefault(Duration.getDefaultInstance())
    }

    @Test
    fun `parse a string`() {
        val expected = randomDuration()
        val str = Durations.toString(expected)
        val converted = Durations2.parse(str)

        converted shouldBe expected
    }

    @Test
    fun `convert to Java Time type and back via 'converter()'`() {
        val original = randomDuration()
        val converted = converter.reverse().convert(original)
        val back = converter.convert(converted)

        back shouldBe original
    }

    @Test
    fun `convert to Java Time type`() {
        val original = randomDuration()
        val backward = converter.reverse()

        toJavaTime(original) shouldBe backward.convert(original)
    }

    @Nested internal inner class
    `provide DSL methods for` {

        private var value: Long = 0

        @BeforeEach
        fun generateValue() {
            value = TestValues.random(10000).toLong()
        }

        @Test
        fun hours() {
            Durations.toHours(hours(value)) shouldBe value
        }

        @Test
        fun minutes() {
            Durations.toMinutes(minutes(value)) shouldBe value
        }

        @Test
        fun seconds() {
            Durations.toSeconds(seconds(value)) shouldBe value
        }

        @Test
        fun milliseconds() {
            Durations.toMillis(milliseconds(100500L)) shouldBe 100500L
        }

        @Test
        fun `hours and minutes`() {
            val hours: Long = 3
            val minutes: Long = 25
            val secondsTotal = hoursToSeconds(hours) + minutesToSeconds(minutes)
            val expected = seconds(secondsTotal)
            val actual = hoursAndMinutes(hours, minutes)

            actual shouldBe expected
        }
    }

    @Nested internal inner class
    `convert a number of hours` {

        private fun test(hours: Long) {
            val expected = seconds(hoursToSeconds(hours))
            val actual = hours(hours)

            actual shouldBe expected
        }

        @Test
        fun `zero value`() {
            test(0)
        }

        @Test
        fun `positive value`() {
            test(36)
        }

        @Test
        fun `negative value`() {
            test(-384)
        }
    }

    @Nested internal inner class
    `fail if` {

        @Test
        fun `hours value is too big`() {
            assertThrows<ArithmeticException> {
                hours(Long.MAX_VALUE)
            }
        }

        @Test
        fun `minutes value is too big`() {
            assertThrows<ArithmeticException> {
                minutes(Long.MAX_VALUE)
            }
        }
    }

    @Nested internal inner class
    `add` {

        @Test
        fun `two 'null's to 'ZERO'`() {
            add(null, null) shouldBe Durations.ZERO
        }

        @Test
        fun `'null' returning same instance`() {
            val duration = seconds(525)

            add(duration, null) shouldBeSameInstanceAs duration
            add(null, duration) shouldBeSameInstanceAs duration
        }

        @Test
        fun `positive durations`() {
            testAddSeconds(25, 5)
            testAddSeconds(300, 338)
        }

        @Test
        fun `negative durations`() {
            testAddSeconds(-25, -5)
            testAddSeconds(-300, -338)
        }

        @Test
        fun `both positive and negative durations`() {
            testAddSeconds(25, -5)
            testAddSeconds(-300, 338)
        }

        private fun testAddSeconds(seconds1: Long, seconds2: Long) {
            val secondsTotal = seconds1 + seconds2
            val sumExpected = seconds(secondsTotal)
            val sumActual = add(seconds(seconds1), seconds(seconds2))

            sumActual shouldBe sumExpected
        }
    }

    @Nested internal inner class
    `Obtain from 'Duration'` {

        @Test
        fun `amount of hours`() {
            Durations.toHours(hoursAndMinutes(10, 40)) shouldBe 10

            Durations.toHours(hoursAndMinutes(-256, -50)) shouldBe -256
        }
    }

    @Nested internal inner class
    `verify if 'Duration' is` {

        @Test
        fun `positive or zero`() {
            isPositiveOrZero(seconds(360)) shouldBe true
            isPositiveOrZero(seconds(0)) shouldBe true
            isPositiveOrZero(seconds(-32)) shouldBe false
        }

        @Test
        fun zero() {
            isZero(seconds(0)) shouldBe true
            isZero(seconds(360)) shouldBe false
            isZero(seconds(-32)) shouldBe false
        }
    }

    @Nested internal inner class
    `tell if 'Duration' is` {

        @Test
        fun greater() {
            isGreaterThan(seconds(64), seconds(2)) shouldBe true
            isGreaterThan(seconds(2), seconds(64)) shouldBe false
            isGreaterThan(seconds(5), seconds(5)) shouldBe false
        }

        @Test
        fun less() {
            isLessThan(seconds(2), seconds(64)) shouldBe true
            isLessThan(seconds(64), seconds(2)) shouldBe false
            isLessThan(seconds(5), seconds(5)) shouldBe false
        }
    }

    companion object {
        private fun randomDuration(): Duration {
            return add(
                seconds(TestValues.random(10000).toLong()),
                nanos(TestValues.random(100000).toLong())
            )
        }

        private fun minutesToSeconds(minutes: Long): Long {
            return minutes * 60L
        }

        private fun hoursToSeconds(hours: Long): Long {
            return hours * 60L * 60L
        }
    }
}
