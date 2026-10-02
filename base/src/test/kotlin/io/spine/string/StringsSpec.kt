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

package io.spine.string

import com.google.protobuf.Timestamp
import com.google.protobuf.stringValue
import com.google.protobuf.timestamp
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.spine.testing.TestValues.randomString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("Extensions for `String` should")
class StringsSpec {

    @ParameterizedTest
    @CsvSource("aaa,Aaa", "field_name,Field_name", "TypeName,TypeName", "_uri,_uri")
    fun `produce a title case string`(initial: String, expected: String) {
        initial.titleCase() shouldBe expected
    }

    @ParameterizedTest
    @CsvSource("aaa,Aaa", "field_name,FieldName", "TypeName,TypeName", "___u_ri____,URi")
    fun `produce a camel case string`(initial: String, expected: String) {
        initial.camelCase() shouldBe expected
    }

    @ParameterizedTest
    @CsvSource("aaa,aaa", "field_name,fieldName", "TypeName,typeName", "___u_ri____,uRi")
    fun `produce a lower camel case string`(initial: String, expected: String) {
        initial.lowerCamelCase() shouldBe expected
    }

    @Test
    fun `trim whitespace`() {
        val value = """
            line one   
             line two 
        """
        // Check that we have a space char at the end after `trimIndent()`.
        value.trimIndent().lines()[0].last() shouldBe ' '

        val trimmed = value.trimWhitespace()

        trimmed.lines()[0].last() shouldBe 'e' // as in "one".

        trimmed shouldBe "line one" + System.lineSeparator() + " line two"
    }

    @Test
    fun `trim indent preserving system line separators`() {
        val value = """
            line 1
            line 2
        """.ti()

        value shouldContain Separator.nl()
    }

    @Test
    fun `prepend indentation preserving system line separator`() {
        val lines = """
          a
           b
            c
        """.trimIndent() // This splits lines with "\n"
           .pi()         // This applies `Separator.NL` prepending with default indentation.

        lines shouldStartWith "    a"
        lines shouldContain Separator.nl()
    }

    @Test
    fun `join iterable with backticks`() {
        listOf("a", "b", "c").joinBackticked() shouldBe "`a`, `b`, `c`"
    }

    @Test
    fun `provide extension for stringifying an object`() {
        val value = "some value"
        value.stringify() shouldBe value
    }

    @Test
    fun `indent lines`() {
        val source = """
            line 1
            line 2
            line 3
        """.ti().lines()

        source.indent(Indent(size = 2), level = 2) shouldBe """
            |    line 1
            |    line 2
            |    line 3
        """.tm()

        source.indent(Indent(size = 3), level = 0) shouldBe """
            |line 1
            |line 2
            |line 3
        """.tm()
    }

    @Test
    fun `encode and decode using Base64`() {
        val original = randomString()
        val encoded: String = original.toBase64Encoded()
        val decoded: String = encoded.decodeBase64()

        decoded shouldBe original
    }

    @Test
    fun `count non-overlapping subscrings`() {
        "baobab".count("mao") shouldBe 0
        "ababababa".count("aba") shouldBe 2
        "the three truths".count("th") shouldBe 3
    }

    @Test
    fun `ensure prefix`() {
        "abc".ensurePrefix("x") shouldBe "xabc"
        "".ensurePrefix("x") shouldBe "x"
        "abc".ensurePrefix("ab") shouldBe "abc"
        assertThrows<IllegalArgumentException> {
            "abc".ensurePrefix("")
        }
    }

    @Nested inner class
    `provide simple class name` {

        @Test
        fun `for type parameter`() {
            simply<Any>() shouldBe "Any"
            simply<Timestamp>() shouldBe "Timestamp"
        }

        @Test
        fun `for instance`() {
            val any = Any()
            val timestamp = timestamp {  }
            any.simpleClassName shouldBe "Any"
            timestamp.simpleClassName shouldBe "Timestamp"
        }

        @Test
        fun `for anonymous object`(){
            val anonymous = object {}
            anonymous.simpleClassName shouldBe CLASS_NAME_UNKNOWN
        }
    }

    @Nested inner class
    `provide qualified class name` {

        @Test
        fun `for type parameter`() {
            qualified<Any>() shouldBe "kotlin.Any"
            qualified<Timestamp>() shouldBe "com.google.protobuf.Timestamp"
        }

        @Test
        fun `for instance`() {
            val any = Any()
            val timestamp = timestamp {  }
            any.qualifiedClassName shouldBe "kotlin.Any"
            timestamp.qualifiedClassName shouldBe "com.google.protobuf.Timestamp"
        }

        @Test
        fun `for anonymous object`() {
            val anonymous = object {}
            anonymous.qualifiedClassName shouldBe CLASS_NAME_UNKNOWN
        }
    }

    /**
     * This test does cover the variety of outputs because
     * the function under the test simply calls [io.spine.type.shortDebugString].
     * We make the function used, contributing to the code coverage.
     */
    @Test
    fun `provide shortcut for 'shortDebugString'`() {
        stringValue { value = "Hey" }.shortly() shouldBe "value: \"Hey\""
    }

    @Test
    fun `provide utility for pluralizing a string`() {
        "dog".pluralize(2) shouldBe "dogs"
        "mouse".pluralize(3, "mice") shouldBe "mice"
        "cat".pluralize(1) shouldBe "cat"
    }
}
