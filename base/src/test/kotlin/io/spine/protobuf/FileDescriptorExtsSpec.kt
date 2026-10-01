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

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.Timestamp
import com.google.protobuf.TimestampProto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertDoesNotThrow

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("`FileDescriptor` extensions for should")
internal class FileDescriptorExtsSpec {

    private val fileDescriptor: FileDescriptor = Timestamp.getDescriptor().file

    @Test
    fun `provide outer class name for a file`() {
        fileDescriptor.outerClassName shouldEndWith "TimestampProto"
    }

    @Test
    fun `provide outer class`() {
        fileDescriptor.outerClass shouldBe TimestampProto::class.java
    }

    @Test
    fun `register extensions with a registry`() {
        val registry = ExtensionRegistry.newInstance()
        assertDoesNotThrow {
            fileDescriptor.registerAllExtensions(registry)
        }
    }
}
