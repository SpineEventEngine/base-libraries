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

package io.spine.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`ComparableStringValue` should")
class ComparableStringValueTest {

    @Test
    @DisplayName("compare")
    @SuppressWarnings("LocalVariableNamingConvention") /* shorter names are meaningful for this test */
    void compare() {
        var a = new TestVal("a");
        var b = new TestVal("b");

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(new TestVal("a")));
    }

    private static class TestVal extends ComparableStringValue<TestVal> {

        @Serial
        private static final long serialVersionUID = 0L;

        private TestVal(String value) {
            super(value);
        }
    }
}
