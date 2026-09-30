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

import com.google.common.collect.ImmutableList;
import io.spine.testing.UtilityClassTest;
import io.spine.value.StringTypeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.List;

import static io.spine.code.proto.CamelCase.convert;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CamelCase utility class should")
class CamelCaseTest extends UtilityClassTest<CamelCase> {

    CamelCaseTest() {
        super(CamelCase.class);
    }

    @Test
    @DisplayName("capitalize words")
    void capitalizeWords() {
        assertConverted("CapitalizeWords", "capitalize_words");
    }

    @Test
    @DisplayName("not lowercase words")
    void doNotLowercaseWords() {
        assertConverted("TestHTTPRequest", "test_HTTP_request");
    }

    private static void assertConverted(String expectedCamelCase, String underscoredName) {
        UnderscoredName name = new UnderName(underscoredName);
        assertEquals(expectedCamelCase, convert(name));
    }

    /**
     * A test value object.
     */
    private static class UnderName extends StringTypeValue implements UnderscoredName {

        @Serial
        private static final long serialVersionUID = 0L;

        private UnderName(String value) {
            super(value);
        }

        @Override
        public List<String> words() {
            return ImmutableList.copyOf(value().split(WORD_SEPARATOR));
        }
    }
}
