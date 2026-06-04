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
