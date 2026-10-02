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
