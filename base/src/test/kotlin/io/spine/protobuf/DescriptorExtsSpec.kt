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

package io.spine.protobuf

import com.google.protobuf.Timestamp
import com.google.protobuf.Timestamp.SECONDS_FIELD_NUMBER
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.test.protobuf.AddressBook
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("`Descriptor` extensions from `io.spine.protobuf` should")
internal class DescriptorExtsSpec {

    @Nested inner class
    `provide a field descriptor by` {

        private val descriptor = Timestamp.getDescriptor()

        @Test
        fun `the field name`() {
            descriptor.field("seconds")!!.name shouldBe "seconds"
        }

        @Test
        fun `a field descriptor by the field number`() {
            descriptor.field(SECONDS_FIELD_NUMBER)!!.number shouldBe SECONDS_FIELD_NUMBER
        }
    }

    @Test
    fun `list nested types excluding synthetic ones for map entries`() {
        val descr = AddressBook.getDescriptor()
        val mapEntryName = "RecentEntry"

        // Ensure the stub type contains a map entry.
        descr.nestedTypes.find { it.name == mapEntryName } shouldNotBe null

        val realNestedTypes = descr.realNestedTypes()
        realNestedTypes.find { it.name == mapEntryName } shouldBe null
        realNestedTypes.map { it.name } shouldContainExactlyInAnyOrder listOf(
            "Contact",
            "PhoneNumber"
        )
    }
}
