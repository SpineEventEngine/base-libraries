/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.environment;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Boolean.TRUE;

/**
 * Encapsulates work with the values of the {@link #KEY} environment variable.
 */
@Immutable
@SuppressWarnings("AccessOfSystemProperties" /* is necessary for this class to function */)
final class TestsProperty {

    /**
     * The key name of the system property which tells if a code runs under a testing framework.
     *
     * <p>If your testing framework is not among the {@link Tests#knownTestingFrameworks()},
     * set this property to {@code true} before running tests.
     */
    @VisibleForTesting
    static final String KEY = "io.spine.tests";

    /**
     * The values for the {@link #KEY} that tells we are in running tests.
     *
     * <p>The case for the values is ignored when checked.
     *
     * @see Tests#enabled()
     */
    @VisibleForTesting
    static final ImmutableList<String> TESTS_VALUES =
            ImmutableList.of(TRUE.toString(), "1");

    /**
     * Surrounding characters that may be in the value.
     */
    private static final Pattern QUOTES_OR_SPACE = Pattern.compile("\"' ");

    /**
     * The value of the {@code System} property with stripped quotes or spaces.
     *
     * <p>Is {@code null} if the property was not set.
     */
    private final @Nullable String value;

    TestsProperty() {
        var propValue = System.getProperty(KEY);
        if (propValue != null) {
            propValue = QUOTES_OR_SPACE.matcher(propValue)
                                       .replaceAll("");
        }
        this.value = propValue;
    }

    /**
     * Verifies if the property has a text value.
     */
    boolean isSet() {
        return value != null;
    }

    /**
     * Transforms the text of the property into a boolean value.
     *
     * @throws IllegalStateException
     *          if called when the value is not {@linkplain #isSet() explicitly set}
     */
    boolean value() {
        checkState(value != null);
        var result = TESTS_VALUES.stream()
                .anyMatch(value::equalsIgnoreCase);
        return result;
    }

    /**
     * Removes the value of the property, if it was previously set.
     */
    static void clear() {
        System.clearProperty(KEY);
    }
}
