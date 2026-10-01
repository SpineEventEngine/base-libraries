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

package io.spine.code.proto;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;

@DisplayName("`LocationPath` should")
class LocationPathTest {

    @Test
    @DisplayName("reject negative items passed")
    void nonNegative() {
        assertIllegalArgument(() -> new LocationPath(1, 2, -1));
    }

    @Test
    @DisplayName("test equality by paths")
    void equality() {
        new EqualsTester()
                .addEqualityGroup(new LocationPath(0, 1, 2), new LocationPath(0, 1, 2))
                .addEqualityGroup(new LocationPath(2, 1, 0))
                .addEqualityGroup(new LocationPath())
                .testEquals();
    }

    @Test
    @DisplayName("print the path to string")
    void text() {
        var assertString = assertThat(new LocationPath(6, 17, 10, 20).toString());
        assertString.contains("6");
        assertString.contains("17");
        assertString.contains("10");
        assertString.contains("20");
    }
}
