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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serial;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`StringTypeValue` should")
class StringTypeValueTest {

    @SuppressWarnings("SerializableInnerClassWithNonSerializableOuterClass")
    @Test
    @DisplayName("return value")
    void getValue() {
        var expected = "return_value_in_toString";

        var value = new StringTypeValue(expected) {
            @Serial
            private static final long serialVersionUID = 0L;
        };

        assertEquals(expected, value.toString());
    }

    @Test
    @DisplayName("have `hashCode()` and `equals()`")
    void hashCodeAndEquals() {
        new EqualsTester().addEqualityGroup(new StrVal("uno"), new StrVal("uno"))
                          .addEqualityGroup(new StrVal("dos"))
                          .testEquals();
    }

    @Test
    @DisplayName("tell if empty")
    void isEmpty() {
        assertTrue(new StrVal("").isEmpty());
        assertFalse(new StrVal(" ").isEmpty());
    }

    @Test
    @DisplayName("be `Serializable`")
    void serialize() {
        reserializeAndAssert(new StrVal(getClass().getName()));
    }

    /** Simple descendant for testing. */
    private static class StrVal extends StringTypeValue {

        @Serial
        private static final long serialVersionUID = 0L;

        StrVal(String value) {
            super(value);
        }
    }
}
