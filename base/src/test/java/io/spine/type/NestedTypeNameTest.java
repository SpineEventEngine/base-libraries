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

import com.google.common.testing.NullPointerTester;
import io.spine.test.type.Uri;
import io.spine.test.type.GreetingServiceProto;
import io.spine.test.type.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`NestedTypeName` should")
class NestedTypeNameTest {

    private static final Type<?, ?> nestedMessage =
            new MessageType(Uri.Authorization.getDescriptor());
    private static final Type<?, ?> nestedEnum = EnumType.create(Uri.Schema.getDescriptor());
    private static final Type<?, ?> topLevelMessage = new MessageType(Uri.getDescriptor());
    private static final Type<?, ?> topLevelEnum = EnumType.create(Language.getDescriptor());
    private static final Type<?, ?> service = ServiceType.of(GreetingServiceProto
                                                                     .getDescriptor()
                                                                     .getServices()
                                                                     .get(0));

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(NestedTypeName.class);
    }

    @Test
    @DisplayName("exist for a nested message type")
    void nestedMessage() {
        check("Uri.Authorization", nestedMessage);
    }

    @Test
    @DisplayName("exist for a nested enum type")
    void nestedEnum() {
        check("Uri.Schema", nestedEnum);
    }

    @Test
    @DisplayName("exist for a top-level message type")
    void message() {
        check("Uri", topLevelMessage);
    }

    @Test
    @DisplayName("exist for a top-level enum type")
    void topLevelEnum() {
        check("Language", topLevelEnum);
    }

    @Test
    @DisplayName("exist for a service type")
    void service() {
        check("GreetingService", service);
    }

    @Test
    @DisplayName("join parts with underscores")
    void printWithUnderscores() {
        Type<?, ?> type = new MessageType(Uri.Protocol.getDescriptor());
        var name = NestedTypeName.of(type);
        assertThat(name.joinWithUnderscore()).isEqualTo("Uri_Protocol");
    }

    private static void check(String expected, Type<?, ?> type) {
        var name = type.nestedSimpleName();
        assertThat(name.value()).isEqualTo(expected);
    }
}
