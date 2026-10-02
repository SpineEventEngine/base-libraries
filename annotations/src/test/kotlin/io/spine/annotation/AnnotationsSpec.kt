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

package io.spine.annotation

import io.kotest.matchers.shouldBe
import java.lang.annotation.RetentionPolicy.RUNTIME
import java.lang.annotation.RetentionPolicy.SOURCE
import java.lang.annotation.RetentionPolicy.CLASS
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * This test suite tests `RetentionPolicy` of the annotations in
 * the `io.spine.annotation` package.
 */
@DisplayName("`io.spine.annotation` package should")
internal class AnnotationsSpec {

    /**
     * Tests that API level annotation classes have the [RUNTIME] retention policy.
     *
     * The [RUNTIME] level is required to:
     *  1. Ease the usage in tests.
     *  2. Allow handling `Internal` types in inbound and outbound communications.
     */
    @Test
    fun `have API level annotations with 'RUNTIME' retention`() {
        arrayOf(
            Beta::class.java,
            Experimental::class.java,
            Internal::class.java,
            SPI::class.java
        ).forEach {
            it.retention() shouldBe RUNTIME
        }
    }

    @Test
    fun `have 'GeneratedMixin' annotation`() {
        GeneratedMixin::class.java.retention() shouldBe SOURCE
    }

    @Test
    fun `have the 'CLASS' retention in the 'Generated' annotation`() {
        Generated::class.java.retention() shouldBe CLASS
    }

    @Test
    fun `have the 'SOURCE' retention in the 'Modified' annotation`() {
        Modified::class.java.retention() shouldBe SOURCE
    }
}

private fun <T: Annotation> Class<in T>.retention() =
    getAnnotation(java.lang.annotation.Retention::class.java).value
