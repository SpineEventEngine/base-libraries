/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
