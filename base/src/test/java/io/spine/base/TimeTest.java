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

import com.google.common.truth.Subject;
import com.google.protobuf.Timestamp;
import io.spine.base.Time.IncrementalNanos;
import io.spine.base.Time.SystemTimeProvider;
import io.spine.base.given.ConstantTimeProvider;
import io.spine.base.given.FakeTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Time.currentTime;
import static io.spine.base.Time.resetProvider;
import static io.spine.base.Time.setProvider;
import static io.spine.base.Time.systemTime;
import static io.spine.base.given.GivenDurations.DURATION_1_MINUTE;
import static io.spine.base.given.GivenDurations.DURATION_5_MINUTES;
import static io.spine.testing.Assertions.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("`Time` class should")
class TimeTest {

    @AfterEach
    void tearDown() {
        resetProvider();
    }

    @Test
    @DisplayName("accept `TimeProvider`")
    void acceptProvider() {
        var fiveMinutesAgo = subtract(currentTime(), DURATION_5_MINUTES);

        setProvider(new ConstantTimeProvider(fiveMinutesAgo));

        assertCurrentTime().isEqualTo(fiveMinutesAgo);
    }

    private static Subject assertCurrentTime() {
        return assertThat(currentTime());
    }

    @Test
    @DisplayName("reset `TimeProvider` to default value")
    void reset() {
        var aMinuteAgo = subtract(systemTime(), DURATION_1_MINUTE);

        setProvider(new ConstantTimeProvider(aMinuteAgo));
        resetProvider();

        assertCurrentTime().isNotEqualTo(aMinuteAgo);
    }

    @Nested
    @DisplayName("Have `SystemTimeProvider`")
    class SystemTime {

        @Test
        @DisplayName("which is singleton")
        void isSingleton() {
            assertNotNull(SystemTimeProvider.INSTANCE);
            assertHasPrivateParameterlessCtor(SystemTimeProvider.class);
        }

        @Test
        @DisplayName("which provides different values for two consecutive calls")
        void differentValuesForConsecutive() {
            assertThat(systemTime()).isNotEqualTo(systemTime());
        }
    }

    @Nested
    @DisplayName("Have an emulator of nanosecond values")
    class IncrementalNanosEmulator {

        @Test
        @DisplayName("which returns incremental emulated values if called several times" +
                " within a single point of wall-clock time")
        void differentValuesForTheSameTime() {
            var now = Instant.now();
            var seconds = now.getEpochSecond();
            var nanos = now.getNano();
            assertThat(IncrementalNanos.valueForTime(seconds, nanos))
                    .isLessThan(IncrementalNanos.valueForTime(seconds, nanos));
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Test
        @DisplayName("which returns zero nanosecond value for each new point in time")
        void resetsNanosForNewInstant() {
            var now = Instant.now();
            var oneMsLater = now.plus(1, ChronoUnit.MILLIS);
            IncrementalNanos
                    .valueForTime(now.getEpochSecond(), now.getNano()); // Ignore this value.
            var oneMsLaterSeconds = oneMsLater.getEpochSecond();
            var oneMsLaterNanos = oneMsLater.getNano();
            var value = IncrementalNanos.valueForTime(oneMsLaterSeconds, oneMsLaterNanos);
            assertThat(value).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("obtain system time even if `TimeProvider` is set")
    void gettingSystemTime() {
        setProvider(new ConstantTimeProvider(Timestamp.getDefaultInstance()));

        assertThat(systemTime().getSeconds()).isNotEqualTo(0);
    }

    @Test
    @DisplayName("obtain current time zone")
    void timeZone() {
        var zoneId = Time.currentTimeZone();
        assertThat(zoneId).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    @DisplayName("obtain the time zone from the provider")
    void customTimeZone() {
        setProvider(new FakeTimeProvider());
        assertThat(Time.currentTimeZone()).isEqualTo(FakeTimeProvider.ZONE);
    }
}
