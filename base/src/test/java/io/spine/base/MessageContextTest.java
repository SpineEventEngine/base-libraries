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

package io.spine.base;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Message;
import io.spine.testing.StubMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("MessageContext interface should")
class MessageContextTest {

    @Test
    @DisplayName("extend Message")
    void contextSuffix() {
        assertThat(Message.class.isAssignableFrom(StubMessageContext.class))
                .isTrue();
    }

    /**
     * Stub implementation of {@link MessageContext}.
     */
    @Immutable
    private static class StubMessageContext extends StubMessage implements MessageContext {
        private static final long serialVersionUID = 0L;
    }
}
