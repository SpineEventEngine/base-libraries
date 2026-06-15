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

package io.spine.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.protobuf.Messages;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.protobuf.Messages.isNotDefault;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utilities for checking preconditions.
 */
@SuppressWarnings("OverloadedVarargsMethod") // for user convenience.
public final class Preconditions2 {

    /** Prevents instantiation of this utility class. */
    private Preconditions2() {
    }

    /**
     * Ensures that the passed string is not {@code null}, empty or blank string.
     *
     * @param str
     *         the string to check
     * @return the passed string
     * @throws IllegalArgumentException
     *         if the string is empty or blank
     * @throws NullPointerException
     *         if the passed string is {@code null}
     */
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String str) {
        checkNotNull(str);
        checkArgument(
                isNotEmpty(str),
                "Non-empty and non-blank string expected. Encountered: \"%s\".", str
        );
        return str;
    }

    private static boolean isNotEmpty(String str) {
        return !str.trim()
                   .isEmpty();
    }

    /**
     * Ensures that the passed string is not {@code null}, empty or blank string.
     *
     * @param str
     *         the string to check
     * @param errorMessage
     *         the exception message to use if the check fails
     * @return the passed string
     * @throws IllegalArgumentException
     *         if the string is empty or blank
     * @throws NullPointerException
     *         if the passed string is {@code null}
     */
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String str, @Nullable Object errorMessage) {
        checkNotNull(str, errorMessage);
        checkArgument(isNotEmpty(str), errorMessage);
        return str;
    }

    /**
     * Ensures that the passed string is not {@code null}, empty or blank string.
     *
     * @param str
     *         the string to check
     * @param errorMessageTemplate
     *         the exception message template to use if the check fails
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @return the passed string
     * @throws IllegalArgumentException
     *         if the string is empty or blank
     * @throws NullPointerException
     *         if the passed string is {@code null}
     */
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String str,
                                              @Nullable String errorMessageTemplate,
                                              @Nullable Object @Nullable ... errorMessageArgs) {
        checkNotNull(str, errorMessageTemplate, errorMessageArgs);
        checkArgument(isNotEmpty(str), errorMessageTemplate, errorMessageArgs);
        return str;
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value
     *         the value to check
     * @throws IllegalArgumentException
     *         if the value is negative or zero
     */
    @CanIgnoreReturnValue
    public static long checkPositive(long value) {
        if (value <= 0) {
            throw newIllegalArgumentException("A positive value expected. Encountered: %d.", value);
        }
        return value;
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value
     *         the value to check
     * @param errorMessageTemplate
     *         the exception message template to use if the check fails
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalArgumentException
     *         if the value is negative or zero
     */
    @CanIgnoreReturnValue
    public static long checkPositive(long value,
                                     @Nullable String errorMessageTemplate,
                                     @Nullable Object @Nullable ... errorMessageArgs) {
        checkArgument(value > 0L, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Ensures that the passed value is positive or zero.
     *
     * @param value
     *         the value to check
     * @throws IllegalArgumentException
     *         if the value is negative
     */
    @CanIgnoreReturnValue
    public static long checkNonNegative(long value) {
        if (value < 0) {
            throw newIllegalArgumentException(
                    "A positive value or zero expected. Encountered: %d.", value
            );
        }
        return value;
    }

    /**
     * Ensures that the passed value is positive or zero.
     *
     * @param value
     *         the value to check
     * @param errorMessageTemplate
     *         the exception message template to use if the check fails
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalArgumentException
     *         if the value is negative
     */
    @CanIgnoreReturnValue
    public static long checkNonNegative(long value,
                                        @Nullable String errorMessageTemplate,
                                        @Nullable Object @Nullable ... errorMessageArgs) {
        checkArgument(value >= 0L, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Ensures that the passed message is not in the default state.
     *
     * @param object
     *         the message to check
     * @param <M>
     *         the type of the message
     * @return the passed message
     * @throws IllegalArgumentException
     *          if the passed message has the default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <M extends @NonNull Message> M checkNotDefaultArg(M object) {
        checkArgument(!Messages.isDefault(object));
        return object;
    }

    /**
     * Ensures that the passed message is not in the default state.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param <M>
     *         the type of the message to check
     * @return the passed message
     * @throws IllegalStateException
     *         if the passed message has the default state
     * @throws NullPointerException
     *         if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <M extends @NonNull Message> M checkNotDefaultState(M object) {
        checkState(!Messages.isDefault(object));
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessage
     *         the message for the exception to be thrown;
     *         will be converted to a string using {@link String#valueOf(Object)}
     * @param <M>
     *         the type of the message to check
     * @throws IllegalStateException
     *         if the object is in its default state
     * @throws NullPointerException
     *         if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <M extends Message>
    M checkNotDefaultArg(M object, @Nullable Object errorMessage) {
        checkNotNull(object, errorMessage);
        checkArgument(isNotDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param <M>
     *         the type of the message to check
     * @param errorMessage
     *         the message for the exception to be thrown;
     *         will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException
     *         if the object is in its default state
     * @throws NullPointerException
     *         if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <M extends Message>
    M checkNotDefaultState(M object, @Nullable Object errorMessage) {
        checkNotNull(object, errorMessage);
        checkState(isNotDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessageTemplate
     *         a template for the exception message should the check fail
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalArgumentException
     *         if the object is in its default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message>
    M checkNotDefaultArg(M object,
                         @Nullable String errorMessageTemplate,
                         @Nullable Object @Nullable ... errorMessageArgs) {
        checkNotNull(object, errorMessageTemplate, errorMessageArgs);
        checkArgument(isNotDefault(object), errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessageTemplate
     *         a template for the exception message should the check fail
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalStateException
     *         if the object is in its default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message>
    M checkNotDefaultState(M object,
                           @Nullable String errorMessageTemplate,
                           @Nullable Object @Nullable ... errorMessageArgs) {
        checkNotNull(object, errorMessageTemplate, errorMessageArgs);
        checkState(isNotDefault(object), errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed value is within the specified range.
     *
     * <p>Both ends of the range are inclusive.
     *
     * @param value
     *         the value to check
     * @param paramName
     *         the name of the parameter that is included into the error messages
     *         if the check fails
     * @param lowBound
     *         the lower bound to check
     * @param highBound
     *         the higher bound
     */
    public static void checkBounds(long value, String paramName, long lowBound, long highBound) {
        checkNotNull(paramName);
        if (!isBetween(value, lowBound, highBound)) {
            throw newIllegalArgumentException(
                    "`%s` (value: %d) must be in bounds [%d, %d] inclusive.",
                    paramName, value, lowBound, highBound);
        }
    }

    private static boolean isBetween(long value, long lowBound, long highBound) {
        return lowBound <= value && value <= highBound;
    }
}
