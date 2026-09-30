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

package io.spine.base;

import io.spine.protobuf.AnyPacker;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.base.Throwables.getStackTraceAsString;

/**
 * Utility class for working with {@link Error}s.
 */
public final class Errors {

    /** Prevents instantiation of this utility class. */
    private Errors() {
    }

    /**
     * Creates a new instance of {@link Error} by the passed {@code Throwable}.
     */
    public static Error fromThrowable(Throwable throwable) {
        var result = toErrorBuilder(throwable);
        return result.build();
    }

    /**
     * Creates an instance by the root cause of the passed {@link Throwable}.
     *
     * @param throwable
     *         the {@code Throwable} to convert
     * @return new instance of {@link Error}
     */
    public static Error causeOf(Throwable throwable) {
        var error = toBuilderCauseOf(throwable);
        return error.build();
    }

    /**
     * Creates an instance by the root cause of the given {@link Throwable} with
     * the given error code.
     *
     * <p>The error code may represent a number in an enum or a native error number.
     *
     * @param throwable
     *         the {@code Throwable} to convert
     * @param errorCode
     *         the error code to include in the resulting {@link Error}
     * @return new instance of {@link Error}
     * @see #causeOf(Throwable) as the recommended overload
     */
    public static Error causeOf(Throwable throwable, int errorCode) {
        var error = toBuilderCauseOf(throwable).setCode(errorCode);
        return error.build();
    }

    private static Error.Builder toBuilderCauseOf(Throwable throwable) {
        return toErrorBuilder(getRootCause(throwable));
    }

    /**
     * Converts the given {@code Throwable} into an {@link Error} builder.
     *
     * <p>The class FQN of the {@code Throwable} becomes the {@code Error.type}.
     *
     * <p>The message of the {@code Throwable} becomes the {@code Error.message}.
     *
     * <p>The {@code Error.stacktrace} is populated by dumping the stacktrace of
     * the {@code Throwable} into a string.
     *
     * <p>If the {@code Throwable} implements {@link ErrorWithMessage},
     * the {@code error} field is populated with the message produced by the throwable.
     *
     * @param throwable
     *         the {@code Throwable} to convert
     * @return new builder of {@link Error}
     */
    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"}) // Calling builder.
    private static Error.Builder toErrorBuilder(Throwable throwable) {
        checkNotNull(throwable);
        var type = throwable.getClass().getCanonicalName();
        var message = nullToEmpty(throwable.getMessage());
        var stacktrace = getStackTraceAsString(throwable);
        var result = Error.newBuilder()
                .setType(type)
                .setMessage(message)
                .setStacktrace(stacktrace);
        if (throwable instanceof ErrorWithMessage) {
            var validationException = (ErrorWithMessage<?>) throwable;
            result.setDetails(AnyPacker.pack(validationException.asMessage()));
        }
        return result;
    }
}
