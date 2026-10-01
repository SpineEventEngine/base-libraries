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

import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;

import java.io.Serial;
import java.text.ParseException;

import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;

/**
 * The default stringifier for {@code Duration}s.
 */
final class DurationStringifier extends SerializableStringifier<Duration> {

    @Serial
    private static final long serialVersionUID = 0L;
    private static final DurationStringifier INSTANCE = new DurationStringifier();

    private DurationStringifier() {
        super("Stringifiers.forDuration()");
    }

    static DurationStringifier getInstance() {
        return INSTANCE;
    }

    @Override
    protected String toString(Duration duration) {
        var result = Durations.toString(duration);
        return result;
    }

    @Override
    protected Duration fromString(String str) {
        Duration result;
        try {
            result = Durations.parse(str);
        } catch (ParseException e) {
            throw illegalArgumentWithCauseOf(e);
        }
        return result;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
