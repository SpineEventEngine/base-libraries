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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.protobuf.Durations2.hoursAndMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`DurationStringifier` should")
class DurationStringifierTest extends AbstractStringifierTest<Duration> {

    DurationStringifierTest() {
        super(Stringifiers.forDuration(), Duration.class);
    }

    @Override
    protected Duration createObject() {
        return hoursAndMinutes(5, 37);
    }

    @Test
    @DisplayName("convert negative duration")
    void convertNegativeDuration() {
        var stringifier = stringifier();
        var negative = hoursAndMinutes(-4, -31);
        assertEquals(negative, parser().convert(stringifier.convert(negative)));
    }
}
