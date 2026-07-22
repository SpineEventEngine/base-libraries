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

package io.spine.base;

import com.google.errorprone.annotations.ThreadSafe;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;
import io.spine.annotation.VisibleForTesting;

import java.time.Instant;
import java.time.ZoneId;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Utilities for working with time information.
 */
public final class Time {

    private static Provider timeProvider = SystemTimeProvider.INSTANCE;

    /** Prevents instantiation of this utility class. */
    private Time() {
    }

    /**
     * Obtains current time via the current {@link Provider}.
     *
     * @return current time
     * @see #setProvider(Provider)
     */
    public static synchronized Timestamp currentTime() {
        var result = timeProvider.currentTime();
        return result;
    }

    /**
     * Obtains system time.
     *
     * <p>The values returned are guaranteed to be different for the consecutive calls in scope
     * of a single JVM. This is achieved by incrementing the default system time millis
     * by the emulated nanosecond value.
     *
     * <p>The nanoseconds are computed in a thread-safe incremental way starting at zero
     * and ending by {@code 999 999}, leaving the millisecond value intact.
     *
     * <p>Unlike {@link #currentTime()}, this method <strong>always</strong> relies on the system
     * milliseconds.
     *
     * @return current system time
     */
    public static Timestamp systemTime() {
        return SystemTimeProvider.INSTANCE.currentTime();
    }

    /**
     * Obtains the current time zone ID.
     *
     * @return the {@link ZoneId} of the current time zone
     */
    public static synchronized ZoneId currentTimeZone() {
        return timeProvider.currentZone();
    }

    /**
     * Sets the provider of the current time.
     *
     * <p>The most common scenario for using this method is test cases of code that deals
     * with current time.
     *
     * @param provider
     *         the provider to set
     */
    @Internal
    @VisibleForTesting
    public static synchronized void setProvider(Provider provider) {
        timeProvider = checkNotNull(provider);
    }

    /**
     * Sets the default current time provider that obtains current time based on the system
     * millis and the emulated nanosecond value.
     *
     * @see #systemTime() for more details
     */
    public static synchronized void resetProvider() {
        timeProvider = SystemTimeProvider.INSTANCE;
    }

    /**
     * The provider of the current time.
     *
     * <p>Implement this interface and pass the resulting class to {@link #setProvider(Provider)}
     * in order to change the {@link Time#currentTime()} results.
     */
    @Internal
    public interface Provider {

        /**
         * Obtains the current time in UTC.
         */
        Timestamp currentTime();

        /**
         * Obtains the current time zone ID.
         *
         * @implSpec The default implementation uses the {@link ZoneId#systemDefault()}
         *         zone.
         */
        default ZoneId currentZone() {
            return ZoneId.systemDefault();
        }
    }

    /**
     * Default implementation of current time provider based on {@link Instant#now()} and
     * the emulated nanosecond value.
     *
     * @see IncrementalNanos for more details on the nanosecond emulation
     */
    @VisibleForTesting
    static class SystemTimeProvider implements Provider {

        @VisibleForTesting
        static final Provider INSTANCE = new SystemTimeProvider();

        /** Prevent instantiation from outside. */
        private SystemTimeProvider() {
        }

        /**
         * {@inheritDoc}
         *
         * <p>Starting from Java 9 the precision of time may differ from platform to platform and
         * depends on the underlying clock used by the JVM. See the corresponding
         * <a href='https://bugs.openjdk.java.net/browse/JDK-8068730'>issue</a> on this matter.
         *
         * <p>In order to provide consistent behavior on different platforms and avoid the overflow
         * of the emulated nanosecond value, this method intentionally uses the system time
         * in millisecond precision. Therefore even if the system clock may offer more precise
         * values, the {@code System.currentTimeMillis()} is used as a base for the returned values.
         */
        @Override
        public Timestamp currentTime() {
            var millis = System.currentTimeMillis();
            var seconds = (millis / 1000);
            @SuppressWarnings("NumericCastThatLosesPrecision")
            var nanos = (int) (millis % 1000) * (int) MILLISECONDS.toNanos(1);
            var nanosOnly = IncrementalNanos.valueForTime(seconds, nanos);
            var result = Timestamp.newBuilder()
                    .setSeconds(seconds)
                    .setNanos(nanos + nanosOnly)
                    .build();
            return result;
        }
    }

    /**
     * Provides an incremental value of nanoseconds for the local JVM.
     *
     * <p>In most cases, the JVM and underlying OS provide the millisecond-level precision at best.
     * Therefore, the messages produced in such a virtual machine are often stamped
     * with the same time value. However, most of the message-ordering routines require
     * the distinct time values for proper work.
     *
     * <p>This class is designed to emulate the nanoseconds and provide incremental values
     * for the consecutive calls.
     *
     * <p>Due to the limitations of the most storage engines, which round the time values to
     * the nearest or the lowest microsecond, the nanosecond values produced by
     * this class are incremented by {@code 1 000} nanoseconds, i.e. by {@code 1} microsecond
     * per call.
     *
     * <p>The returned nanosecond value starts at {@code 0} and never exceeds {@code 999 999}.
     * It is designed to keep the millisecond value provided by a typical-JVM system clock intact.
     *
     * <p>The nanosecond value is reset for each new passed {@code seconds} and {@code nanos}
     * values. That allows receiving {@code 1 000} distinct time values per millisecond.
     *
     * <p>In case the upper bound of the nanos is reached, meaning that there were more than
     * {@code 1 000} calls to this class within a millisecond, the nanosecond value is reset
     * back to {@code 0}.
     */
    @ThreadSafe
    static final class IncrementalNanos {

        private static final int MAX_VALUE = 1_000_000;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private static final int NANOS_PER_MICROSECOND = (int) MICROSECONDS.toNanos(1);

        private static final IncrementalNanos instance = new IncrementalNanos();

        private int counter;

        private long previousSeconds;
        private int previousNanos;

        private synchronized int getNextValue(long seconds, int nanos) {
            if (previousSeconds == seconds && previousNanos == nanos) {
                counter += NANOS_PER_MICROSECOND;
                counter = counter % MAX_VALUE;
            } else {
                counter = 0;
            }
            previousSeconds = seconds;
            previousNanos = nanos;
            return counter;
        }

        /**
         * Obtains the next nanosecond value.
         */
        static int valueForTime(long seconds, int nanos) {
            return instance.getNextValue(seconds, nanos);
        }
    }
}
