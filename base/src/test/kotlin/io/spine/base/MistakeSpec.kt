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

package io.spine.base

import io.kotest.matchers.shouldBe
import java.io.Serial
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Mistake` should")
internal class MistakeSpec {

    @Test
    fun `provide no arg constructor`() {
        KMistake().let {
            it.message shouldBe null
            it.cause shouldBe null
        }
    }

    @Test
    fun `have a cause`() {
        val cause = Exception("The cause")
        KMistake(cause).let {
            it.cause shouldBe cause
            it.message shouldBe cause.toString()
        }
    }

    @Test
    fun `have a message`() {
        val message = "The message"
        KMistake(message).let {
            it.message shouldBe message
            it.cause shouldBe null
        }
    }

    @Test
    fun `have both cause and message`() {
        val message = "Tango"
        val cause = Exception("Triplet")
        KMistake(message, cause).let {
            it.message shouldBe message
            it.cause shouldBe cause
        }
    }
}

private class KMistake : Mistake {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 0L
    }
}
