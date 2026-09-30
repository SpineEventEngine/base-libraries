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

package io.spine.io

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`Closeable` interface should")
internal class CloseableSpec {

    @Test
    fun `tell if the object is open`() {
        Window(true).isOpen shouldBe true
        Window(false).isOpen shouldBe false
    }

    @Test
    fun `close the object`() {
        with (Window(true)) {
            close()
            isOpen shouldBe false
        }
    }

    @Test
    fun `ensure that the object is open`() {
        assertThrows<IllegalStateException> {
            Window(false).checkOpen()
        }
    }

    @Test
    fun `close the object, if open`() {
        with(Window(true)) {
            isOpen shouldBe true
            closeIfOpen()
            isOpen shouldBe false
        }
        with(Window(false)) {
            isOpen shouldBe false
            assertDoesNotThrow { closeIfOpen() }
            isOpen shouldBe false
        } 
    }
}

private class Window(private var state: Boolean) : Closeable {

    override val isOpen: Boolean
        get() = state

    override fun close() {
        state = false
    }
}
