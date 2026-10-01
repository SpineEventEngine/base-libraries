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

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING;
import static io.spine.code.proto.FieldTypes.keyDescriptor;
import static io.spine.code.proto.FieldTypes.valueDescriptor;
import static io.spine.code.proto.given.Given.enumField;
import static io.spine.code.proto.given.Given.mapField;
import static io.spine.code.proto.given.Given.messageField;
import static io.spine.code.proto.given.Given.primitiveField;
import static io.spine.code.proto.given.Given.repeatedField;
import static io.spine.code.proto.given.Given.singularField;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`FieldTypes` utility should")
class FieldTypesTest extends UtilityClassTest<FieldTypes> {

    FieldTypesTest() {
        super(FieldTypes.class);
    }

    @Nested
    @DisplayName("check if a field is")
    class CheckIfField {

        @Test
        @DisplayName("a `Message`")
        void isMessage() {
            assertTrue(FieldTypes.isMessage(messageField()));
            assertFalse(FieldTypes.isMessage(primitiveField()));
            assertFalse(FieldTypes.isMessage(enumField()));
        }

        @Test
        @DisplayName("`repeated`")
        void isRepeated() {
            assertTrue(FieldTypes.isRepeated(repeatedField()));
            assertFalse(FieldTypes.isRepeated(singularField()));
        }

        @Test
        @DisplayName("a `Map`")
        void isMap() {
            assertTrue(FieldTypes.isMap(mapField()));
            assertFalse(FieldTypes.isMap(singularField()));
        }
    }

    @Test
    @DisplayName("not mark map field as `repeated`")
    void notMarkMapAsRepeated() {
        assertFalse(FieldTypes.isRepeated(mapField()));
    }

    @Test
    @DisplayName("get key descriptor for a `Map` field")
    void getKeyDescriptor() {
        var key = keyDescriptor(mapField());
        assertEquals(INT64, key.getType());
    }

    @Test
    @DisplayName("get value descriptor for a `Map` field")
    void getValueDescriptor() {
        var value = valueDescriptor(mapField());
        assertEquals(STRING, value.getType());
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` if")
    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
            // Calling methods to throw exception.
    class Prohibit {

        @Test
        @DisplayName("getting key descriptor from non-map field")
        void getKeyForNonMap() {
            assertIllegalArgument(() -> keyDescriptor(repeatedField()));
        }

        @Test
        @DisplayName("getting value descriptor from non-map field")
        void getValueForNonMap() {
            assertIllegalArgument(() -> valueDescriptor(repeatedField()));
        }
    }
}
