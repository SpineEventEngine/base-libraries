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

import com.google.common.collect.ImmutableList;
import com.google.common.truth.StringSubject;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.string.Diags.COMMA_AND_SPACE;
import static io.spine.string.Diags.backtick;
import static io.spine.string.Diags.toEnumeration;
import static io.spine.string.Diags.toEnumerationBackticked;

@DisplayName("`Diags` utility should")
class DiagsTest extends UtilityClassTest<Diags> {

    DiagsTest() {
        super(Diags.class);
    }

    @Test
    @DisplayName("backtick string representation of an object")
    void backticks() {
        Object anObject = getClass();
        var backticked = backtick(anObject);

        var assertOutput = assertThat(backticked);
        assertOutput.startsWith("`");
        assertOutput.endsWith("`");
        assertOutput.contains(anObject.toString());
    }

    @Nested
    @DisplayName("join")
    class Joining {

        @Test
        @DisplayName("`Iterable`")
        void iterable() {
            List<String> items = ImmutableList.of("one", "two", "tree");
            var joined = Diags.join(items);

            var assertOutput = assertThat(joined);
            assertOutput.contains(COMMA_AND_SPACE);
            items.forEach(assertOutput::contains);
        }

        @Test
        @DisplayName("vararg")
        void varArg() {
            var joined = Diags.join("uno", "dos", "tres");

            var assertOutput = assertThat(joined);
            ImmutableList.of("uno", "dos", "tres")
                         .forEach(assertOutput::contains);
        }

        @Test
        @DisplayName("separating with comma followed by space char")
        void commaThenSpace() {
            var joined = Diags.join(100, 200, 300);

            var assertOutput = assertThat(joined);
            assertOutput.contains(COMMA_AND_SPACE);
            ImmutableList.of(100, 200, 300)
                         .forEach(item -> assertOutput.contains(item.toString()));
        }
    }

    @Nested
    @DisplayName("provide collector to comma-separated string")
    class Collectors {

        private final ImmutableList<String> list = ImmutableList.of("foo", "bar", "baz");
        private StringSubject assertOutput;

        @Test
        @DisplayName("with items")
        void stringEnumeration() {
            var output = list.stream()
                    .collect(toEnumeration());

            assertOutput = assertThat(output);

            assertOutput.contains(COMMA_AND_SPACE);
            list.forEach(assertOutput::contains);
        }

        @Test
        @DisplayName("with backticked items")
        void backtickedEnumeration() {
            var output = list.stream()
                    .collect(toEnumerationBackticked());

            assertOutput = assertThat(output);

            assertOutput.contains(COMMA_AND_SPACE);
            list.forEach(item -> assertOutput.contains(backtick(item)));
        }
    }
}
