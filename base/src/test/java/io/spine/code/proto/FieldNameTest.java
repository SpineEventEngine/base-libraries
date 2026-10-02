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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FieldName should")
class FieldNameTest {

    private static final String PROTO_FIELD_NAME = "correct_java_name";

    private final FieldName fieldName = FieldName.of(PROTO_FIELD_NAME);
    private final FieldName fieldNameWithNumbers = FieldName.of("hand22hand");

    @Nested
    @DisplayName("obtain CamelCase")
    class CamelCase {

        @Test
        @DisplayName("of lower-cased letters")
        void lowerCasedLetters() {
            assertCamelCase("CorrectJavaName", fieldName);
        }

        @Test
        @DisplayName("of lower-cased letters with a number")
        void lowerCasedLettersAndNumbers() {
            assertCamelCase("Hand22Hand", fieldNameWithNumbers);
        }

        @Test
        @DisplayName("of capitalized name")
        void capitalizedName() {
            assertCamelCase("TypeURLString", FieldName.of("type_URL_string"));
        }

        private void assertCamelCase(String expectedCamelCase, FieldName fieldName) {
            assertEquals(expectedCamelCase, fieldName.toCamelCase());
        }
    }

    @Nested
    @DisplayName("obtain javaCase")
    class JavaCase {

        @Test
        @DisplayName("of lower-cased letters")
        void lowerCasedLetters() {
            assertJavaCase("correctJavaName", fieldName);
        }

        @Test
        @DisplayName("of lower-cased letters with a number")
        void lowerCasedLettersAndNumbers() {
            assertJavaCase("hand22Hand", fieldNameWithNumbers);
        }

        private void assertJavaCase(String expectedJavaCase, FieldName fieldName) {
            assertEquals(expectedJavaCase, fieldName.javaCase());
        }
    }
}
