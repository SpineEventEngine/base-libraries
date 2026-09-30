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

import com.google.common.base.Joiner;
import io.spine.annotation.Internal;
import io.spine.annotation.VisibleForTesting;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for debug and error diagnostics.
 */
@Internal
public final class Diags {

    @VisibleForTesting
    static final String COMMA_AND_SPACE = ", ";
    private static final Joiner JOINER = Joiner.on(COMMA_AND_SPACE);
    private static final char BACKTICK = '`';

    /** Prevents instantiation of this utility class. */
    private Diags() {
    }

    /**
     * Wraps the string representation of the passed object into backticks.
     */
    public static String backtick(Object object) {
        checkNotNull(object);
        return BACKTICK + object.toString() + BACKTICK;
    }

    /**
     * Lists the passed items separating with a comma followed by a space character.
     */
    public static String join(Iterable<?> items) {
        checkNotNull(items);
        return JOINER.join(items);
    }

    /**
     * Lists the passed elements separating with a comma followed by a space character.
     */
    @SafeVarargs
    public static <E> String join(E... elements) {
        checkNotNull(elements);
        return JOINER.join(elements);
    }

    /**
     * Returns a {@code Collector} that enumerates items separating their string
     * representation with a comma followed by a space character.
     */
    public static Collector<Object, ?, String> toEnumeration() {
        return Collectors.mapping(String::valueOf, Collectors.joining(COMMA_AND_SPACE));
    }

    /**
     * Returns a {@code Collector} that backticks string representations of the passed
     * items and joins items into a string, separating with a comma followed
     * by a space character.
     */
    public static Collector<Object, ?, String> toEnumerationBackticked() {
        return Collectors.mapping(Diags::backtick, toEnumeration());
    }
}
