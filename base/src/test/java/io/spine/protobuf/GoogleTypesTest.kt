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

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.EnumValue
import io.kotest.matchers.shouldBe
import io.spine.type.KnownTypes
import io.spine.type.TypeName
import org.junit.jupiter.api.Test

internal class GoogleTypesTest {

    private val protoFiles = listOf(
        /*

         Starting from v3.22.0 Google Protobuf for Java (`protobuf-java-3.22.0.jar`) no longer
         contains the `plugin.proto`. The file is still present in the Protobuf source code tree
         under the `protobuf/src/google/protobuf/compiler/` directory, but it does not seem to be
         present in artifacts produced for Java.

         The references to `plugin.proto` are present in build files for C++. So, it could be an
         omission in the v3.22 build, or Protobuf authors may see that this rarely used proto file
         can be included in the source code tree for code generators written in other languages.
         We still want the latest version of the Protobuf library and the `protoc` compiler.
         So, using `plugin.proto` as a source file copy under `tool-base` is a backup option for us.

         Please uncomment the below line if the file appears back in the Protobuf distribution
         archive for Java.

         "google/protobuf/compiler/plugin.proto",

        */

        "google/protobuf/any.proto",
        "google/protobuf/api.proto",
        "google/protobuf/descriptor.proto",
        "google/protobuf/duration.proto",
        "google/protobuf/empty.proto",
        "google/protobuf/field_mask.proto",
        "google/protobuf/source_context.proto",
        "google/protobuf/struct.proto",
        "google/protobuf/timestamp.proto",
        "google/protobuf/type.proto",
        "google/protobuf/wrappers.proto"
    )

    /**
     * Verifies that `EnumValue` protobuf type is known for the production JVM code.
     *
     * `io.spine.protobuf.EnumConverter` deals with `com.google.protobuf.EnumValue`,
     * but `google.protobuf.EnumValue` type is not used in our proto code.
     *
     * This test ensures the type is available via descriptors stored in resources.
     *
     * Since this module depends on `base` on the `implementation` level,
     * tests in this module see `base` as if it is used as a library.
     */
    @Test
    fun `make sure 'EnumValue' is known to production code`() {
        val type = TypeName.of("google.protobuf.EnumValue")
        val cls = type.toJavaClass()

        cls shouldBe EnumValue::class.java
    }

    @Test
    fun `all files from the Protobuf library are included`() {
        val protobufFiles = KnownTypes.instance().files()
            .filter { f -> f.name.startsWith("google/protobuf/") }
            .map { it.name }

        // Use Truth assertion because it prints missing items if the assertion fails.
        assertThat(protobufFiles).containsAtLeastElementsIn(protoFiles)
    }
}
