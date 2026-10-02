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
