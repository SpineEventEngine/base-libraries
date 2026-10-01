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

import static java.lang.Character.toUpperCase;

/**
 * Utilities for working with {@code CamelCapitalization}.
 */
final class CamelCase {

    /** Prevent instantiation of this utility class. */
    private CamelCase() {
    }

    /**
     * Converts an underscored name to {@code CamelCase} string.
     *
     * <p>Does not force lowercase conversion so that {@code "test_HTTP_request"} would become
     * {@code "TestHTTPRequest"}.
     */
    static String convert(UnderscoredName name) {
        var iterator = name.words().iterator();
        var builder = new StringBuilder(name.value().length());
        while (iterator.hasNext()) {
            var word = iterator.next();
            if (!word.isEmpty()) {
                builder.append(toUpperCase(word.charAt(0)))
                       .append(word.substring(1));
            }
        }
        return builder.toString();
    }
}
