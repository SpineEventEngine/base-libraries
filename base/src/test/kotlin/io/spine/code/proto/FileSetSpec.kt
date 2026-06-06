/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.code.proto

import com.google.common.testing.EqualsTester
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Empty
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`FileSet` should")
internal class FileSetSpec {

    private lateinit var fileSet: FileSet

    @BeforeEach
    fun loadFileSet() {
        fileSet = FileSet.load()
    }

    @Nested internal inner class
    `verify if it contains a file` {

        @Test
        fun `by its name`() {
            val name = FileName.from(Empty.getDescriptor().file)
            fileSet.contains(name) shouldBe true
        }

        @Test
        fun `by a collection of names`() {
            val name = FileName.from(Empty.getDescriptor().file)
            fileSet.containsAll(listOf(name)) shouldBe true
        }
    }

    @Nested internal inner class
    `find a file` {

        @Test
        fun `by its name`() {
            val name = FileName.from(Empty.getDescriptor().file)
            fileSet.tryFind(name).shouldBePresent()
        }

        @Test
        fun `by a collection of names`() {
            val name = FileName.from(Empty.getDescriptor().file)
            val found = fileSet.find(listOf(name))
            found.size() shouldBe 1
        }
    }

    @Test
    fun `be empty when newly created`() {
        val emptySet = FileSet.newInstance()
        emptySet.isEmpty shouldBe true
        emptySet.size() shouldBe 0
    }

    @Test
    fun `create a union with another set`() {
        val emptySet = FileSet.newInstance()
        fileSet.union(emptySet) shouldBe fileSet
        emptySet.union(fileSet) shouldBe fileSet

        val anotherSet = FileSet.newInstance()
        val file = Empty.getDescriptor().file
        anotherSet.add(file)

        val union = emptySet.union(anotherSet)
        union.size() shouldBe 1
        union.contains(FileName.from(file)) shouldBe true
    }

    @Test
    fun `create a union of two non-empty sets`() {
        val setA = FileSet.newInstance()
        setA.add(Empty.getDescriptor().file)
        val setB = FileSet.newInstance()
        setB.add(com.google.protobuf.Any.getDescriptor().file)

        val union = setA.union(setB)

        union.size() shouldBe 2
        union.contains(FileName.from(Empty.getDescriptor().file)) shouldBe true
        union.contains(FileName.from(com.google.protobuf.Any.getDescriptor().file)) shouldBe true
    }

    @Test
    fun `construct from file descriptor protos`() {
        val fileSet = FileSet.of(listOf(Empty.getDescriptor().file.toProto()))
        fileSet.contains(FileName.from(Empty.getDescriptor().file)) shouldBe true
    }

    @Test
    fun `parse a descriptor set file`() {
        val descriptorSet = FileDescriptorSet.newBuilder()
            .addFile(Empty.getDescriptor().file.toProto())
            .build()
        val file = File.createTempFile("file-set", ".desc")
        file.deleteOnExit()
        file.writeBytes(descriptorSet.toByteArray())

        val parsed = FileSet.parse(file)

        parsed.contains(FileName.from(Empty.getDescriptor().file)) shouldBe true
    }

    @Test
    fun `parse a descriptor set file resolving files via known types`() {
        // `descriptor.proto` declares many types, exercising the duplicate-key
        // merge when grouping known types by their file.
        val descriptorProtoFile = FileDescriptorSet.getDescriptor().file
        val descriptorSet = FileDescriptorSet.newBuilder()
            .addFile(descriptorProtoFile.toProto())
            .build()
        val file = File.createTempFile("known-files", ".desc")
        file.deleteOnExit()
        file.writeBytes(descriptorSet.toByteArray())

        val parsed = FileSet.parseAsKnownFiles(file)

        parsed.contains(FileName.from(descriptorProtoFile)) shouldBe true
    }

    @Test
    fun `filter files by predicate`() {
        val filtered = fileSet.filter { it.fullName.contains("empty") }
        filtered.files().forEach {
            it.fullName.contains("empty") shouldBe true
        }
    }

    @Test
    fun `convert to array`() {
        val array = fileSet.toArray()
        array.size shouldBe fileSet.size()
    }

    @Test
    fun `support 'equals()' and 'hashCode()'`() {
        val file = Empty.getDescriptor().file
        val set1 = FileSet.newInstance()
        set1.add(file)
        val set2 = FileSet.newInstance()
        set2.add(file)

        EqualsTester()
            .addEqualityGroup(set1, set2)
            .addEqualityGroup(FileSet.newInstance())
            .addEqualityGroup(fileSet)
            .testEquals()
    }

    @Test
    fun `provide 'toString'`() {
        val file1 = Empty.getDescriptor().file
        val file2 = com.google.protobuf.Any.getDescriptor().file
        val set = FileSet.newInstance()
        set.add(file1)
        set.add(file2)

        val str = set.toString()
        str shouldContain "FileSet"
        str shouldContain "files="
        str shouldContain file1.fullName
        str shouldContain file2.fullName

        // google/protobuf/any.proto comes before google/protobuf/empty.proto
        str.indexOf(file2.fullName) shouldBeLessThan str.indexOf(file1.fullName)
    }
}
