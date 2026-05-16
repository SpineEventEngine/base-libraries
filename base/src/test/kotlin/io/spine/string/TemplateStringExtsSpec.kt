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

package io.spine.string

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`TemplateString` extensions should")
internal class TemplateStringExtsSpec {

    @Nested inner class
    `format the template string` {

        @Test
        fun `returning the correct result`() {
            val template = templateString {
                withPlaceholders = "My dog's name is \${dog.name}."
                placeholderValue["dog.name"] = "Fido"
            }
            template.format() shouldBe "My dog's name is Fido."
        }

        @Test
        fun `substituting multiple placeholders`() {
            val template = templateString {
                withPlaceholders = "\${greeting}, \${name}!"
                placeholderValue["greeting"] = "Hello"
                placeholderValue["name"] = "World"
            }
            template.format() shouldBe "Hello, World!"
        }

        @Test
        fun `substituting the same placeholder more than once`() {
            val template = templateString {
                withPlaceholders = "\${word} \${word} \${word}"
                placeholderValue["word"] = "tick"
            }
            template.format() shouldBe "tick tick tick"
        }

        @Test
        fun `returning an empty string if given an empty template`() {
            TemplateString.getDefaultInstance().format() shouldBe ""
        }

        @Test
        fun `returning the template as-is when no placeholders are present`() {
            val template = templateString {
                withPlaceholders = "Just a plain string."
            }
            template.format() shouldBe "Just a plain string."
        }

        @Test
        fun `throwing when a placeholder has no value`() {
            assertThrows<IllegalArgumentException> {
                val template = templateString {
                    withPlaceholders = "My dog's name is \${dog.name}."
                }
                template.format()
            }
        }

        @Test
        fun `ignore when a placeholder with a value is not used`() {
            assertDoesNotThrow {
                val template = templateString {
                    withPlaceholders = "My dog's name is Fido."
                    placeholderValue["dog.name"] = "Fido"
                }
                template.format()
            }
        }
    }

    @Nested inner class
    `validate the template against placeholders` {

        private val message = { missingPlaceholders: List<Placeholder> -> "$missingPlaceholders" }
        private val templateText = "\${val1}, \${val2}, \${val3}, \${val4}, \${val5}"
        private val fooPlaceholders = mapOf("val1" to "Foo", "val2" to "Foo", "val3" to "Foo")
        private val barPlaceholders = mapOf("val4" to "Bar", "val5" to "Bar")

        @Test
        fun `failing if the template has non-presentable placeholder`() {
            val template = templateString {
                withPlaceholders = templateText
                placeholderValue.putAll(fooPlaceholders)
            }
            val exception = assertThrows<IllegalArgumentException> {
                template.requireComplete(message)
            }
            exception.message shouldBe message(listOf(Placeholder("val4"), Placeholder("val5")))
        }

        @Test
        fun `bypassing the template if all placeholders are present`() {
            val template = templateString {
                withPlaceholders = templateText
                placeholderValue.putAll(fooPlaceholders + barPlaceholders)
            }
            assertDoesNotThrow {
                template.requireComplete(message)
            }
        }

        @Test
        fun `accepting an empty template`() {
            assertDoesNotThrow {
                TemplateString.getDefaultInstance().requireComplete()
            }
        }
    }

    @Nested inner class
    `extract placeholders from the template` {

        @Test
        fun `returning every occurrence in the order they appear`() {
            val template = "\${a} and \${b} and \${a}"
            Placeholder.extractPlaceholders(template) shouldBe
                listOf(Placeholder("a"), Placeholder("b"), Placeholder("a"))
        }

        @Test
        fun `preserving the order of repeated placeholders across the template`() {
            val template = "\${b}-\${a}-\${b}-\${c}-\${a}-\${b}"
            Placeholder.extractPlaceholders(template) shouldBe listOf(
                Placeholder("b"),
                Placeholder("a"),
                Placeholder("b"),
                Placeholder("c"),
                Placeholder("a"),
                Placeholder("b"),
            )
        }

        @Test
        fun `returning an empty list for a plain string`() {
            Placeholder.extractPlaceholders("no placeholders here") shouldBe emptyList()
        }

        @Test
        fun `supporting dotted, underscored, and mixed identifiers`() {
            val template = "\${my.key}-\${my_key}-\${myKey}"
            Placeholder.extractPlaceholders(template) shouldBe
                listOf(Placeholder("my.key"), Placeholder("my_key"), Placeholder("myKey"))
        }
    }

    @Nested inner class
    `join placeholders into a quoted, comma-separated string` {

        @Test
        fun `wrapping each name in backticks and separating with a comma and a space`() {
            val placeholders = listOf(Placeholder("foo"), Placeholder("bar"), Placeholder("baz"))
            placeholders.joinQuoted() shouldBe "`foo`, `bar`, `baz`"
        }

        @Test
        fun `producing a single backticked name for one element`() {
            listOf(Placeholder("only")).joinQuoted() shouldBe "`only`"
        }

        @Test
        fun `producing an empty string for an empty iterable`() {
            emptyList<Placeholder>().joinQuoted() shouldBe ""
        }

        @Test
        fun `preserving the order of elements and supporting dotted or underscored names`() {
            val placeholders = listOf(
                Placeholder("my.key"),
                Placeholder("my_key"),
                Placeholder("myKey"),
            )
            placeholders.joinQuoted() shouldBe "`my.key`, `my_key`, `myKey`"
        }
    }

    @Test
    fun `format with missing placeholders`() {
        val template = templateString {
            withPlaceholders = "My dog's name is \${dog.name} and its breed is \${dog.breed}."
            placeholderValue["dog.name"] = "Fido"
        }
        template.formatUnsafe() shouldBe "My dog's name is Fido and its breed is \${dog.breed}."
    }

    @Nested inner class
    `resolve placeholders referenced from other placeholder values` {

        @Test
        fun `via 'format', resolving a transitive chain`() {
            val template = templateString {
                withPlaceholders = "\${greeting}"
                placeholderValue["greeting"] = "Hello, \${name}!"
                placeholderValue["name"] = "World"
            }
            template.format() shouldBe "Hello, World!"
        }

        @Test
        fun `via 'format', not depending on map iteration order`() {
            // A value containing placeholder-looking text must not be re-substituted
            // based on the order keys happen to be iterated.
            val template = templateString {
                withPlaceholders = "\${a} then \${b}"
                placeholderValue["a"] = "\${b}"
                placeholderValue["b"] = "literal-b"
            }
            template.format() shouldBe "literal-b then literal-b"
        }

        @Test
        fun `via 'format', throwing on a reference cycle`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "\${b}"
                placeholderValue["b"] = "\${a}"
            }
            val exception = assertThrows<IllegalArgumentException> { template.format() }
            exception.message shouldContain "Cyclic"
            exception.message shouldContain "`a`"
            exception.message shouldContain "`b`"
        }

        @Test
        fun `via 'format', throwing on a three-step reference cycle`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "\${b}"
                placeholderValue["b"] = "\${c}"
                placeholderValue["c"] = "\${a}"
            }
            val exception = assertThrows<IllegalArgumentException> { template.format() }
            exception.message shouldContain "`a` -> `b` -> `c` -> `a`"
        }

        @Test
        fun `via 'formatUnsafe', leaving a three-step reference cycle unresolved`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "\${b}"
                placeholderValue["b"] = "\${c}"
                placeholderValue["c"] = "\${a}"
            }
            template.formatUnsafe() shouldBe "\${a}"
        }

        @Test
        fun `via 'format', throwing on a self-reference cycle`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "x-\${a}-x"
            }
            assertThrows<IllegalArgumentException> { template.format() }
        }

        @Test
        fun `via 'format', throwing on a transitively missing reference`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "see \${b}"
            }
            val exception = assertThrows<IllegalArgumentException> { template.format() }
            exception.message shouldContain "`b`"
        }

        @Test
        fun `via 'formatUnsafe', resolving a transitive chain`() {
            val template = templateString {
                withPlaceholders = "\${greeting}"
                placeholderValue["greeting"] = "Hello, \${name}!"
                placeholderValue["name"] = "World"
            }
            template.formatUnsafe() shouldBe "Hello, World!"
        }

        @Test
        fun `via 'formatUnsafe', leaving a reference cycle unresolved`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "\${b}"
                placeholderValue["b"] = "\${a}"
            }
            // The cycle is broken by leaving the repeated key as literal text.
            template.formatUnsafe() shouldBe "\${a}"
        }

        @Test
        fun `via 'formatUnsafe', leaving a transitively missing reference as-is`() {
            val template = templateString {
                withPlaceholders = "\${a}"
                placeholderValue["a"] = "see \${b}"
            }
            template.formatUnsafe() shouldBe "see \${b}"
        }
    }
}
