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

package io.spine.code.java

import io.kotest.matchers.shouldBe
import io.spine.test.base.rejections.TestRejections
import io.spine.test.type.GreetingServiceProto
import io.spine.testing.Assertions.assertIllegalArgument
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`SimpleClassName` should")
internal class SimpleClassNameSpec {

    @Test
    fun `reject an empty value`() {
        assertIllegalArgument { SimpleClassName.create("") }
    }

    @Test
    fun `append a suffix`() {
        SimpleClassName.create("Order").with("Id") shouldBe SimpleClassName.create("OrderId")
    }

    @Test
    fun `obtain the declared outer class name when the option is set`() {
        val file = GreetingServiceProto.getDescriptor()

        val declared = SimpleClassName.declaredOuterClassName(file)

        declared.isPresent shouldBe true
        declared.get() shouldBe SimpleClassName.create("GreetingServiceProto")
    }

    @Test
    fun `return an empty optional when the outer class name is not declared`() {
        val file = TestRejections.getDescriptor()

        SimpleClassName.declaredOuterClassName(file).isPresent shouldBe false
    }

    @Test
    fun `derive the outer class name from the file name`() {
        val file = TestRejections.getDescriptor()

        SimpleClassName.outerOf(file) shouldBe SimpleClassName.create("TestRejections")
    }
}
