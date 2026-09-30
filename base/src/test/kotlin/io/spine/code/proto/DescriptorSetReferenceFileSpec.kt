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

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.spine.io.Resource
import java.io.File
import java.nio.file.Files
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`DescriptorSetReferenceFile` should")
class DescriptorSetReferenceFileSpec {

    private val classLoader = this::class.java.classLoader

    @Test
    fun `load references from provided resources`() {
        val url = Resource.file("duplicate_entries.ref", classLoader).locate()
        val resources = DescriptorSetReferenceFile.loadFromResources(listOf(url))

        // Expect duplicates removed and comments/blank lines filtered out.
        resources.shouldHaveSize(2)

        val file1 = Resource.file("stub_file_1.desc", classLoader)
        val file2 = Resource.file("known_types.desc", classLoader)
        val resourceSet = resources.toSet()
        resourceSet.contains(file1).shouldBeTrue()
        resourceSet.contains(file2).shouldBeTrue()
    }

    @Test
    fun `create a reference file pointing at the target descriptor set`() {
        val dir = Files.createTempDirectory("ref-file").toFile()
        val target = File("some/dir/known_types.desc")

        DescriptorSetReferenceFile.create(dir, target)

        val written = File(dir, DescriptorSetReferenceFile.NAME)
        written.exists().shouldBeTrue()
        written.readText() shouldBe target.name
    }
}
