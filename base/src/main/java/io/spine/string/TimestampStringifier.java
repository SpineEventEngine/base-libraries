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

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

import java.text.ParseException;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * The stringifier of timestamps into RFC 3339 date string format.
 */
final class TimestampStringifier extends SerializableStringifier<Timestamp> {

    private static final long serialVersionUID = 0L;
    private static final TimestampStringifier INSTANCE = new TimestampStringifier();

    private TimestampStringifier() {
        super("Stringifiers.forTimestamp()");
    }

    static TimestampStringifier getInstance() {
        return INSTANCE;
    }

    @Override
    protected String toString(Timestamp value) {
        return Timestamps.toString(value);
    }

    @Override
    protected Timestamp fromString(String str) {
        try {
            return Timestamps.parse(str);
        } catch (ParseException e) {
            throw newIllegalArgumentException(e.getMessage(), e);
        }
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
