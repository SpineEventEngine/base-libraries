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

import com.google.common.testing.EqualsTester
import com.google.protobuf.Any
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Duration
import io.kotest.matchers.collections.containOnly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain as shouldContainString
import io.spine.type.MessageType
import io.spine.type.TypeName
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`TypeSet` should")
internal class TypeSetTest {

    @Test
    fun `obtain messages and enums from a file`() {
        val fileName = FileName.of("google/protobuf/descriptor.proto")
        /* The file is present in resources. */
        val file = fileSet.tryFind(fileName).get()
        val typeSet = TypeSet.from(file)

        assertNotEmpty(typeSet)
        val expectedTypeName = TypeName.from(FileDescriptorSet.getDescriptor())
        typeSet.contains(expectedTypeName) shouldBe true
    }

    @Test
    @DisplayName("obtain message and enums")
    fun fromSet() {
        val typeSet = TypeSet.from(fileSet)
        assertNotEmpty(typeSet)

        // We have a number of test service declarations for testing annotations.
        typeSet.serviceTypes().shouldNotBeEmpty()
    }

    @Test
    fun `obtain message types from a 'TypeSet'`() {
        val messageTypes = TypeSet.onlyMessages(fileSet)

        messageTypes shouldContain MessageType.of(Any.getDefaultInstance())
        messageTypes shouldContain MessageType.of(Duration.getDefaultInstance())
    }

    @Test
    fun `obtain message types from a file descriptor`() {
        val fileName = FileName.of("google/protobuf/any.proto")
        val file = fileSet.tryFind(fileName).get()
        val messageTypes = TypeSet.onlyMessages(file)

        messageTypes should containOnly(MessageType.of(Any.getDefaultInstance()))
    }

    @Test
    fun `add message types in bulk via the builder`() {
        val anyType = MessageType.of(Any.getDefaultInstance())
        val durationType = MessageType.of(Duration.getDefaultInstance())
        val typeSet = TypeSet.newBuilder()
            .addAll(listOf(anyType, durationType))
            .build()

        typeSet.messageTypes() shouldContain anyType
        typeSet.messageTypes() shouldContain durationType
    }

    @Test
    fun `support equality`() {
        val first = TypeSet.from(fileSet)
        val second = TypeSet.from(fileSet)
        val empty = TypeSet.newBuilder().build()

        EqualsTester()
            .addEqualityGroup(first, second)
            .addEqualityGroup(empty)
            .testEquals()
    }

    @Test
    fun `print its type names`() {
        val typeSet = TypeSet.from(fileSet)
        typeSet.toString() shouldContainString "messageTypes"
    }

    private fun assertNotEmpty(typeSet: TypeSet) {
        typeSet.isEmpty shouldBe false
        typeSet.allTypes().shouldNotBeEmpty()
        typeSet.messageTypes().shouldNotBeEmpty()
        typeSet.enumTypes().shouldNotBeEmpty()
    }

    companion object {
        private val fileSet = FileSet.load()
    }
}
