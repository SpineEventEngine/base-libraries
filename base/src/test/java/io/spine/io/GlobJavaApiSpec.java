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

package io.spine.io;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@DisplayName("`Glob` Java API should expose")
class GlobJavaApiSpec {

    /** The test subject. */
    private Glob glob;

    @Test
    @DisplayName("`any` pattern")
    void anyPattern() {
        assertThat(Glob.any.matches(Paths.get(".")))
                .isTrue();
    }

    @Nested
    @DisplayName("`extensions()` method with")
    class ExtensionsMethod {

        @Test
        @DisplayName("`vararg` parameter")
        void varArg() {
            glob = Glob.extension(".bar", ".b");
            assertMatches("f.bar");
            assertMatches("baz.b");
        }

        @Test
        @DisplayName("`Iterable` parameter")
        void iterableArg() {
            glob = Glob.extension(ImmutableList.of("cc", "h", "hpp", "cpp"));
            assertMatches("format.cc");
            assertMatches("sprintf.h");
        }
    }

    @Nested
    @DisplayName("`extensionLowerAndUpper()` method with")
    class ExtensionLowerAndUpperMethod {

        @Test
        @DisplayName("`vararg` parameter")
        void varArg() {
            glob = Glob.extensionLowerAndUpper("high", "LOW");
            assertMatches("1.high");
            assertMatches("2.HIGH");
            assertMatches("3.low");
            assertMatches("4.LOW");
        }

        @Test
        @DisplayName("`Iterable` parameter")
        void iterableParam() {
            glob = Glob.extensionLowerAndUpper(".snake", "CASE");
            assertMatches("1.snake");
            assertMatches("2.SNAKE");
            assertMatches("3.case");
            assertMatches("4.CASE");
        }
    }

    private void assertMatches(String fileName) {
        var p = Paths.get(fileName);
        var matches = glob.matches(p);
        assertWithMessage(
                "The file `%s` should match the pattern `%s`.", fileName, glob.getPattern()
        ).that(matches)
         .isTrue();
    }
}
