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

package io.spine.type;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.testing.StubMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`UnpublishedLanguageException` should")
class UnpublishedLanguageExceptionTest {

    @Test
    @DisplayName("contain the name of the type in its message")
    void containTypeName() {
        var msg = new SecretMessage();

        assertThat(SecretMessage.class.isAnnotationPresent(Internal.class))
                .isTrue();

        var typeName = TypeName.of(msg);
        var exception = new UnpublishedLanguageException(msg);

        assertThat(exception.getMessage()).contains(typeName.toString());
    }

    /**
     * A stub implementation of the {@code Message} interface, which is
     * annotated as internal, and tries to pretend being {@code Any}
     * so that its type name can be obtained.
     */
    @Internal
    private static class SecretMessage extends StubMessage {

        @Override
        public Message getDefaultInstanceForType() {
            return Any.getDefaultInstance();
        }

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return Any.getDescriptor();
        }
    }
}
