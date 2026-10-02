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

import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Linker` should")
internal class LinkerSpec {

    /** A file without dependencies, hence trivially resolvable. */
    private val independent: FileDescriptorProto = FileDescriptorProto.newBuilder()
        .setName("independent.proto")
        .build()

    /** A file that depends on a file absent from the input set. */
    private val withMissingDependency: FileDescriptorProto = FileDescriptorProto.newBuilder()
        .setName("partial.proto")
        .addDependency("missing.proto")
        .build()

    /** A file that depends on both a present and an absent file. */
    private val withMixedDependencies: FileDescriptorProto = FileDescriptorProto.newBuilder()
        .setName("mixed.proto")
        .addDependency("independent.proto")
        .addDependency("missing.proto")
        .build()

    @Test
    fun `partially resolve files referencing missing dependencies`() {
        val linker = Linker(listOf(independent, withMissingDependency, withMixedDependencies))
        linker.resolve()

        linker.resolved().contains(FileName.of("independent.proto")) shouldBe true
        linker.partiallyResolved().contains(FileName.of("partial.proto")) shouldBe true
        linker.remaining().isEmpty() shouldBe true
    }

    @Test
    fun `expose its state via the string representation`() {
        val linker = Linker(listOf(independent, withMissingDependency))
        linker.resolve()

        val printed = linker.toString()
        printed shouldContain "input"
        printed shouldContain "resolved"
        printed shouldContain "independent.proto"
    }

    @Test
    fun `wrap descriptor validation errors into an illegal state exception`() {
        val duplicateTypes = FileDescriptorProto.newBuilder()
            .setName("duplicate.proto")
            .addMessageType(DescriptorProto.newBuilder().setName("Foo"))
            .addMessageType(DescriptorProto.newBuilder().setName("Foo"))
            .build()

        shouldThrow<IllegalStateException> {
            Linker.link(listOf(duplicateTypes))
        }
    }

    @Nested inner class
    `link a collection of files` {

        @Test
        fun `keeping independent files resolved`() {
            val result = Linker.link(listOf(independent))
            result.contains(FileName.of("independent.proto")) shouldBe true
        }
    }
}
