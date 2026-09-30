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

package io.spine.string;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`EnumStringifier` should")
class EnumStringifierTest {

    @Test
    @DisplayName("convert values to String and back")
    void convert() {
        var stringifier = new EnumStringifier<>(DayOfWeek.class);
        var reverse = stringifier.reverse();

        for (var value : DayOfWeek.values()) {
            var str = stringifier.convert(value);
            assertEquals(value, reverse.convert(str));
        }
    }

    @Test
    @DisplayName("have an identity tied to the name of a processed class")
    void provideDefaultIdentity() {
        var stringifier = new EnumStringifier<>(DayOfWeek.class);
        var identity = stringifier.toString();

        var expected = EnumStringifier.identity(DayOfWeek.class);
        assertThat(identity).isEqualTo(expected);
    }

    private enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY,
        THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
