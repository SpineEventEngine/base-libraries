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

import com.google.protobuf.StringValue
import io.kotest.matchers.shouldBe
import io.spine.protobuf.AnyPacker
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Errors` should")
internal class ErrorsWithMessageSpec {

    private class FailureWithDetails :
        RuntimeException("failed"), ErrorWithMessage<StringValue> {
        override fun asMessage(): StringValue = StringValue.of("the-details")
    }

    @Test
    fun `include details from a throwable carrying an error message`() {
        val error = Errors.fromThrowable(FailureWithDetails())

        val details = AnyPacker.unpack(error.details, StringValue::class.java)
        details.value shouldBe "the-details"
    }
}
