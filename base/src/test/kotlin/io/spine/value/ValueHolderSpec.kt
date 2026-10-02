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

package io.spine.value

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ValueHolder` should")
internal class ValueHolderSpec {

    /**
     * This test ensures that [ValueHolder] has its method [ValueHolder.value] non-final.
     *
     * This is needed for casting of return type values in classes that derive
     * from [ValueHolder] in `core-java`.
     */
    @Test
    fun `have overridable 'value()' method`() {
        @Suppress("serial")
        val stub = object: ValueHolder<String>(javaClass.name) {
            override fun value(): String {
                return value
            }
        }
        stub.value() shouldBe javaClass.name
    }
}
