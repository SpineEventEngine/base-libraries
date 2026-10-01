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

package io.spine.base.given;

import com.google.protobuf.Duration;

public class GivenDurations {

    private static final int SECONDS_IN_1_MINUTE = 60;
    private static final int SECONDS_IN_5_MINUTES = 5 * SECONDS_IN_1_MINUTE;

    public static final Duration DURATION_1_MINUTE = newDuration(SECONDS_IN_1_MINUTE);
    public static final Duration DURATION_5_MINUTES = newDuration(SECONDS_IN_5_MINUTES);

    private static Duration newDuration(int seconds) {
        return Duration.newBuilder()
                       .setSeconds(seconds)
                       .build();
    }

    /** Prevents instantiation of this test environment. */
    private GivenDurations() {
    }
}
