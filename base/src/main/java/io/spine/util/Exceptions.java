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

package io.spine.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.getRootCause;
import static java.lang.String.format;

/**
 * Utility class for working with exceptions for cases that are not
 * covered by {@link com.google.common.base.Throwables Throwables} class from Guava.
 */
public final class Exceptions {

    /** Prevent instantiation of this utility class. */
    private Exceptions() {
    }

    /**
     * Always throws {@code UnsupportedOperationException} initialized with the passed string.
     *
     * <p>Use this method in combination with static import for brevity of code for
     * unsupported operations.
     * The return type is given to keep Java type system happy when called in methods with
     * return type as shown below:
     *
     * <pre>
     *  {@code
     *   import static io.spine.util.Exceptions.unsupported;
     *   ...
     *   T doSomething() {
     *      throw unsupported("Cannot do this");
     *   }
     * }</pre>
     *
     * @param message the message for exception
     * @return nothing ever
     * @throws UnsupportedOperationException always
     */
    public static UnsupportedOperationException unsupported(String message) {
        checkNotNull(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Always throws {@code UnsupportedOperationException} initialized with the formatted string.
     *
     * <p>Use this method in combination with static import for brevity of code for
     * unsupported operations.
     * The return type is given to keep Java type system happy when called in methods with
     * return type as shown below:
     *
     * <pre>
     *  {@code
     *
     *   import static io.spine.util.Exceptions.unsupported;
     *   ...
     *   T doSomething() {
     *      throw unsupported("This operation is not supported because of %s and %s", arg1, arg2);
     *   }
     * }</pre>
     *
     * @param format the format string
     * @param args   formatting parameters
     * @return nothing ever
     * @throws UnsupportedOperationException always
     */
    public static UnsupportedOperationException unsupported(String format, Object... args) {
        checkNotNull(format);
        checkNotNull(args);
        var msg = formatMessage(format, args);
        return unsupported(msg);
    }

    /**
     * Always throws {@code UnsupportedOperationException}.
     *
     * <p>Use this method in combination with static import for brevity of code for
     * unsupported operations.
     * The return type is given to keep Java type system happy when called in methods with
     * return type as shown below:
     *
     * <pre>
     *   import static static io.spine.util.Exceptions.unsupported;
     *   ...
     *   T doSomething() {
     *      throw unsupported();
     *   }
     * </pre>
     *
     * @return nothing ever
     * @throws UnsupportedOperationException always
     */
    @SuppressWarnings("NewExceptionWithoutArguments") // No message is necessary for this case.
    public static UnsupportedOperationException unsupported() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a throwable's cause as the cause of a {@link IllegalStateException} and throws it.
     *
     * @param throwable to wrap
     * @return nothing ever, always throws an exception, the return type is for convenience
     * @throws IllegalStateException always
     */
    public static IllegalStateException illegalStateWithCauseOf(Throwable throwable) {
        checkNotNull(throwable);
        var rootCause = getRootCause(throwable);
        throw new IllegalStateException(rootCause);
    }

    /**
     * Sets a throwable's cause as the cause of a {@link IllegalArgumentException} and throws it.
     *
     * @param throwable to wrap
     * @return nothing ever, always throws an exception, the return type is for convenience
     * @throws IllegalArgumentException always
     */
    public static IllegalArgumentException illegalArgumentWithCauseOf(Throwable throwable) {
        checkNotNull(throwable);
        var rootCause = getRootCause(throwable);
        throw new IllegalArgumentException(rootCause);
    }

    private static String formatMessage(String format, Object[] args) {
        checkNotNull(format);
        checkNotNull(args);
        return format(Locale.ROOT, format, args);
    }

    /**
     * Throws {@code IllegalArgumentException} with the formatted string.
     *
     * @param format the format string
     * @param args   formatting parameters
     * @return nothing ever, always throws an exception. The return type is given for convenience.
     * @throws IllegalArgumentException always
     */
    public static IllegalArgumentException newIllegalArgumentException(String format,
                                                                       Object... args) {
        var errMsg = formatMessage(format, args);
        throw new IllegalArgumentException(errMsg);
    }

    /**
     * Throws {@code IllegalArgumentException} with the formatted string and the cause.
     *
     * @param cause the cause of the exception
     * @param format the format string
     * @param args formatting parameters
     * @return nothing ever, always throws an exception. The return type is given for convenience.
     * @throws IllegalArgumentException always
     */
    public static IllegalArgumentException newIllegalArgumentException(Throwable cause,
                                                                       String format,
                                                                       Object... args) {
        checkNotNull(cause);
        var errMsg = formatMessage(format, args);
        throw new IllegalArgumentException(errMsg, cause);
    }

    /**
     * Throws {@code IllegalStateException} with the formatted string.
     *
     * @param format the format string
     * @param args formatting parameters
     * @return nothing ever, always throws an exception. The return type is given for convenience.
     * @throws IllegalStateException always
     */
    public static IllegalStateException newIllegalStateException(String format,
                                                                 Object... args) {
        var errMsg = formatMessage(format, args);
        throw new IllegalStateException(errMsg);
    }

    /**
     * Throws {@code IllegalStateException} with the formatted string and the cause.
     *
     * @param cause the cause of the exception
     * @param format the format string
     * @param args formatting parameters
     * @return nothing ever, always throws an exception. The return type is given for convenience.
     * @throws IllegalStateException always
     */
    @CanIgnoreReturnValue
    public static IllegalStateException newIllegalStateException(Throwable cause,
                                                                 String format,
                                                                 Object... args) {
        checkNotNull(cause);
        var errMsg = formatMessage(format, args);
        throw new IllegalStateException(errMsg, cause);
    }
}
