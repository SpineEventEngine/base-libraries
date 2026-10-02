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

import io.kotest.matchers.shouldBe
import io.spine.string.Indent.Companion.DEFAULT_JAVA_INDENT_SIZE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Indent` should")
internal class IndentSpec {

    @Test
    fun `have default size`() {
        Indent().value.length shouldBe DEFAULT_JAVA_INDENT_SIZE
    }

    @Test
    fun `return its value in 'toString'`() {
        val indent = Indent(1)
        indent.toString() shouldBe indent.value
    }

    @Test
    fun `obtain indentation at given level`() {
        Indent(2).atLevel(2) shouldBe " ".repeat(4)
    }

    @Test
    fun `provide instance for default indentation in Java`() {
        Indent.defaultJavaIndent.toString() shouldBe " ".repeat(DEFAULT_JAVA_INDENT_SIZE)
    }
}
