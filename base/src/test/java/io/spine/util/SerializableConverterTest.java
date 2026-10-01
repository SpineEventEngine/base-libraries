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

import com.google.common.testing.SerializableTester;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.text.ParseException;

@DisplayName("SerializableConverter should")
class SerializableConverterTest {

    @Test
    @DisplayName("serialize")
    void serialize() {
        SerializableTester.reserialize(new StubSerializer());
    }

    private static class StubSerializer extends SerializableConverter<Timestamp, String> {

        @Serial
        private static final long serialVersionUID = 0L;

        @Override
        protected String doForward(Timestamp timestamp) {
            return Timestamps.toString(timestamp);
        }

        @Override
        protected Timestamp doBackward(String s) {
            try {
                return Timestamps.parse(s);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
