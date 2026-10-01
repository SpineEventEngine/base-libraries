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

import io.spine.test.identifiers.UuidMessage;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`MessageIdToString` utility class should")
class MessageIdToStringTest extends UtilityClassTest<MessageIdToString> {

    MessageIdToStringTest() {
        super(MessageIdToString.class);
    }

    @Test
    @DisplayName("convert `Message` to `String`")
    void convert() {
        var test = UuidMessage.newBuilder()
                .setUuid("0bd2d85f-8a07-4041-a62a-6852654d44e6")
                .build();
        var value = MessageIdToString.convert(test);

        assertThat(value).contains(test.getUuid());
    }
}
