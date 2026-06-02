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
package io.spine.code.proto

import com.google.common.testing.NullPointerTester
import com.google.common.truth.BooleanSubject
import com.google.common.truth.Truth.assertThat
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`proto.PackageName` should")
internal class PackageNameSpec {

    @Test
    fun `handle 'null' arguments`() {
        NullPointerTester().testAllPublicStaticMethods(PackageName::class.java)
    }

    @Test
    fun `provide separator character`() {
        PackageName.delimiter() shouldNotBe ""
    }

    @Test
    fun `create a new instance by value`() {
        val packageName = "some.pack.age"
        PackageName.of(packageName).value() shouldBe packageName
    }

    @Nested internal inner class
    `verify if the package is inner to a parent package` {

        @Test
        fun `if immediately nested`() {
            assertIsInner("spine.code.proto", "spine.code")
        }

        @Test
        fun `if nested deeper`() {
            assertIsInner("spine.code.proto.ref", "spine")
        }

        @Test
        fun `returning 'false' if not`() {
            assertInner("spine.code.proto", "spine.code.java").isFalse()
        }

        private fun assertIsInner(inner: String, outer: String) {
            val assertInner = assertInner(inner, outer)
            assertInner.isTrue()
        }

        private fun assertInner(inner: String, outer: String): BooleanSubject {
            val innerPackage = PackageName.of(inner)
            val outerPackage = PackageName.of(outer)
            return assertThat(innerPackage.isInnerOf(outerPackage))
        }
    }
}
