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
import com.google.protobuf.DescriptorProtos.FieldOptions
import com.google.protobuf.GeneratedMessage.GeneratedExtension
import io.spine.option.OptionsProto
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`AbstractOption` should")
internal class AbstractOptionSpec {

    @Test
    fun `support equality based on the extension number`() {
        EqualsTester()
            .addEqualityGroup(option(OptionsProto.column), option(OptionsProto.column))
            .addEqualityGroup(option(OptionsProto.required))
            .addEqualityGroup(option(OptionsProto.setOnce))
            .testEquals()
    }
}

/**
 * Creates an [AbstractOption] over the given boolean [FieldOptions] extension.
 */
private fun option(extension: GeneratedExtension<FieldOptions, Boolean>): AbstractOption<*, *, *> =
    StubFieldOption(extension)

/**
 * A minimal concrete [FieldOption] exposing the `protected` constructor for the test.
 */
private class StubFieldOption(extension: GeneratedExtension<FieldOptions, Boolean>) :
    FieldOption<Boolean>(extension)
