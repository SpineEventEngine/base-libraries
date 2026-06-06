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
