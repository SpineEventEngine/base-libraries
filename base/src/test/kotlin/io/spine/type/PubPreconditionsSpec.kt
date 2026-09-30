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

package io.spine.type

import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import io.spine.annotation.Internal
import io.spine.given.type.ExplicitInternalType
import io.spine.given.type.ExplicitNonInternalType
import io.spine.given.type.ImplicitInternalType
import io.spine.testing.StubMessage
import java.lang.IllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`PubPreconditions` should")
internal class PubPreconditionsSpec {

    @Test
    fun `require a type to be internal`() {

        fun assertThrowsOn(msg: Message) = assertThrows<IllegalArgumentException> {
            requireInternal(msg)
        }

        fun assertDoesNotThrow(msg: Message) = assertDoesNotThrow {
            requireInternal(msg)
        }

        assertThrowsOn(ExplicitNonInternalType.getDefaultInstance())
        assertThrowsOn(Timestamp.getDefaultInstance())

        assertDoesNotThrow(ExplicitInternalType.getDefaultInstance())
        assertDoesNotThrow(ImplicitInternalType.getDefaultInstance())
        assertDoesNotThrow(StubInternalMsg())
    }

    @Test
    fun `require a type to be published`() {

        fun assertThrowsOn(msg: Message) = assertThrows<UnpublishedLanguageException> {
            requirePublished(msg)
        }

        fun assertDoesNotThrow(msg: Message) = assertDoesNotThrow {
            requirePublished(msg)
        }

        assertThrowsOn(ExplicitInternalType.getDefaultInstance())
        assertThrowsOn(ImplicitInternalType.getDefaultInstance())

        assertDoesNotThrow(ExplicitNonInternalType.getDefaultInstance())
        assertDoesNotThrow(Timestamp.getDefaultInstance())
    }
}

@Internal
private class StubInternalMsg: StubMessage()
