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

package io.spine.base

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.EnumValue
import io.spine.base.IdType.ENUM
import io.spine.base.IdType.MESSAGE
import io.spine.base.IdType.STRING
import io.spine.test.identifiers.TaskStatus
import org.junit.jupiter.api.Test

class `'IdType' should` {

    @Test
    fun `pass 'Message' instance in conversion as is`() {
        val wrapped = STRING.toMessage(Identifier.newUuid())

        assertThat(MESSAGE.toMessage(wrapped))
            .isSameInstanceAs(wrapped)
        assertThat(MESSAGE.fromMessage(wrapped))
            .isSameInstanceAs(wrapped)
    }

    @Test
    fun `convert a Protobuf enum to 'EnumValue'`() {
        val message = ENUM.toMessage(TaskStatus.TASK_OPEN)

        assertThat(message).isInstanceOf(EnumValue::class.java)
        message as EnumValue
        assertThat(message.name).isEqualTo("TASK_OPEN")
        assertThat(message.number).isEqualTo(TaskStatus.TASK_OPEN.number)
    }
}
