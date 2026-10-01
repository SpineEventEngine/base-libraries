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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Time.currentTime;
import static io.spine.string.Stringifiers.fromString;
import static io.spine.testing.Assertions.assertIllegalArgument;

@DisplayName("`TimestampStringifier` should")
class TimestampStringifierTest extends AbstractStringifierTest<Timestamp> {

    TimestampStringifierTest() {
        super(Stringifiers.forTimestamp(), Timestamp.class);
    }

    @Override
    protected Timestamp createObject() {
        return currentTime();
    }

    @Test
    @DisplayName("throw `IllegalArgumentException` when parsing unsupported format")
    void parsingError() {
        // This uses TextFormat printing, for the output that won't be parsable.
        var time = currentTime().toString();
        assertIllegalArgument(() -> fromString(time, Timestamp.class));
    }
}
