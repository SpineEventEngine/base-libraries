/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
