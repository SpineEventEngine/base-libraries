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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import io.spine.string.Stringifiers;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Durations.compare;
import static com.google.protobuf.util.Durations.fromMillis;
import static com.google.protobuf.util.Durations.fromNanos;
import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Durations.toMillis;
import static java.util.Objects.requireNonNull;

/**
 * Utility class for working with durations in addition to those available from the
 * {@link com.google.protobuf.util.Durations Durations} class in the Protobuf Util library.
 *
 * <p>Use {@code import static io.spine.protobuf.Durations2.*} for compact initialization
 * like this:
 * <pre>
 *     {@code
 *     Duration d = add(hours(2), minutes(30));
 *     }
 * </pre>
 *
 * @see com.google.protobuf.util.Durations Durations
 */
@SuppressWarnings("UtilityClass")
public final class Durations2 {

    /** Prevent instantiation of this utility class. */
    private Durations2() {
    }

    /*
     * Methods for brief computations with Durations like
     *       add(hours(2), minutes(30));
     ******************************************************/

    /**
     * Obtains an instance of {@code Duration} representing the passed number of nanoseconds.
     *
     * @param nanos
     *         the number of nanoseconds, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration nanos(long nanos) {
        return fromNanos(nanos);
    }

    /**
     * Obtains an instance of {@code Duration} representing the passed number of milliseconds.
     *
     * @param milliseconds
     *         the number of milliseconds, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration milliseconds(long milliseconds) {
        return fromMillis(milliseconds);
    }

    /**
     * Obtains an instance of {@code Duration} representing the passed number of seconds.
     *
     * @param seconds
     *         the number of seconds, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration seconds(long seconds) {
        return fromSeconds(seconds);
    }

    /**
     * This method allows for more compact code of creation of
     * {@code Duration} instance with minutes.
     */
    public static Duration minutes(long minutes) {
        return Durations.fromMinutes(minutes);
    }

    /**
     * This method allows for more compact code of creation of
     * {@code Duration} instance with hours.
     */
    public static Duration hours(long hours) {
        return Durations.fromHours(hours);
    }

    /**
     * Adds two durations one of which or both can be {@code null}.
     *
     * <p>This method supplements
     * the {@linkplain com.google.protobuf.util.Durations#add(Duration, Duration) utility} from
     * Protobuf Utils for accepting {@code null}s.
     *
     * @param d1
     *         a duration to add, could be {@code null}
     * @param d2
     *         another duration to add, could be {@code null}
     * @return <ul>
     *             <li>sum of two durations if both of them are {@code non-null}
     *             <li>another {@code non-null} value, if one is {@code null}
     *             <li>{@link Durations#ZERO} if both values are {@code null}
     *         </ul>
     * @see com.google.protobuf.util.Durations#add(Duration, Duration)
     */
    public static Duration add(@Nullable Duration d1, @Nullable Duration d2) {
        if (d1 == null && d2 == null) {
            return Durations.ZERO;
        }
        if (d1 == null) {
            return d2;
        }
        if (d2 == null) {
            return d1;
        }
        var result = Durations.add(d1, d2);
        return result;
    }

    /**
     * This method allows for more compact code of creation of
     * {@code Duration} instance with hours and minutes.
     */
    public static Duration hoursAndMinutes(long hours, long minutes) {
        var result = add(hours(hours), minutes(minutes));
        return result;
    }

    /**
     * Returns {@code true} of the passed value is greater or equal zero,
     * {@code false} otherwise.
     */
    public static boolean isPositiveOrZero(Duration value) {
        checkNotNull(value);
        var millis = toMillis(value);
        var result = millis >= 0;
        return result;
    }

    /** Returns {@code true} if the passed value is zero, {@code false} otherwise. */
    public static boolean isZero(Duration value) {
        checkNotNull(value);
        return Durations.ZERO.equals(value);
    }

    /**
     * Returns {@code true} if the first argument is greater than the second,
     * {@code false} otherwise.
     */
    public static boolean isGreaterThan(Duration value, Duration another) {
        var result = compare(value, another) > 0;
        return result;
    }

    /**
     * Returns {@code true} if the first argument is less than the second,
     * {@code false} otherwise.
     */
    public static boolean isLessThan(Duration value, Duration another) {
        var result = compare(value, another) < 0;
        return result;
    }

    /**
     * Converts the passed Java Time value.
     */
    public static Duration of(java.time.Duration value) {
        checkNotNull(value);
        var result = converter().convert(value);
        return requireNonNull(result);
    }

    /**
     * Converts the passed value to Java Time value.
     */
    public static java.time.Duration toJavaTime(Duration value) {
        checkNotNull(value);
        var result =
                converter().reverse()
                           .convert(value);
        return requireNonNull(result);
    }

    /**
     * Parses the string with a duration.
     *
     * <p>Unlike {@link com.google.protobuf.util.Durations#parse(String) its Protobuf counterpart},
     * this method does not throw a checked exception.
     *
     * @throws IllegalArgumentException
     *         if the string is not of required format
     */
    public static Duration parse(String str) {
        checkNotNull(str);
        var result = Stringifiers.forDuration()
                                 .reverse()
                                 .convert(str);
        return requireNonNull(result);
    }

    /**
     * Obtains the instance of Java Time converter.
     */
    public static Converter<java.time.Duration, Duration> converter() {
        return JtConverter.INSTANCE;
    }

    /**
     * Converts from Java Time {@code Duration} to Protobuf {@code Duration} and back.
     */
    private static final class JtConverter
            extends Converter<java.time.Duration, Duration> implements Serializable {

        @Serial
        private static final long serialVersionUID = 0L;

        private static final JtConverter INSTANCE = new JtConverter();

        @Override
        protected Duration doForward(java.time.Duration duration) {
            return Duration.newBuilder()
                    .setSeconds(duration.getSeconds())
                    .setNanos(duration.getNano())
                    .build();
        }

        @Override
        protected java.time.Duration doBackward(Duration duration) {
            return java.time.Duration.ofSeconds(duration.getSeconds(), duration.getNanos());
        }

        @Override
        public String toString() {
            return "Durations2.converter()";
        }

        @Serial
        private Object readResolve() {
            return INSTANCE;
        }
    }
}
