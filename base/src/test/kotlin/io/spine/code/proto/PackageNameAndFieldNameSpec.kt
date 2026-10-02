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

import com.google.protobuf.Timestamp
import io.kotest.matchers.shouldBe
import io.spine.test.type.Url
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`PackageName` and `FieldName` should")
internal class PackageNameAndFieldNameSpec {

    @Test
    fun `obtain a package name from a message descriptor`() {
        val packageName = PackageName.of(Timestamp.getDescriptor())
        packageName.value() shouldBe "google.protobuf"
    }

    @Test
    fun `convert a field name to a single-segment path`() {
        val path = FieldName.of("host").asPath()
        path.fieldNameList shouldBe listOf("host")
    }

    @Test
    fun `obtain a package name of a custom type`() {
        val packageName = PackageName.of(Url.getDescriptor())
        packageName.value() shouldBe "spine.test.type"
    }
}
