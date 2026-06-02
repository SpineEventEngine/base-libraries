/*
 * Copyright 2025, TeamDev. All rights reserved.
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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.test.base.rejections.TestRejections.FlyingObjectUnidentified
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`FieldContext` should")
class FieldContextKtSpec {

    @Test
    fun `create an empty context`() {
        val context = FieldContext.empty()
        context shouldNotBe null
        context.fieldPath().fieldNameList.size shouldBe 0
    }

    @Test
    fun `create context for a field`() {
        val field = FlyingObjectUnidentified.getDescriptor().findFieldByName("plus_code")
        val context = FieldContext.create(field)

        context.target() shouldBe field
        context.fieldPath().fieldNameList.size shouldBe 1
        context.fieldPath().getFieldName(0) shouldBe "plus_code"
    }

    @Test
    fun `create context for a child field`() {
        val field = FlyingObjectUnidentified.getDescriptor().findFieldByName("plus_code")
        val parent = FieldContext.empty()
        val child = parent.forChild(field)

        child.target() shouldBe field
        child.fieldPath().fieldNameList.size shouldBe 1
    }

    @Test
    fun `compare contexts`() {
        val field = FlyingObjectUnidentified.getDescriptor().findFieldByName("plus_code")
        val c1 = FieldContext.create(field)
        val c2 = FieldContext.create(field)

        c1 shouldBe c2
        c1.hashCode() shouldBe c2.hashCode()
    }
}
