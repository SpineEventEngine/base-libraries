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

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.base.Time;

import java.time.ZoneId;

public final class FakeTimeProvider implements Time.Provider {

    public static final Timestamp TIME = Timestamps.EPOCH;
    public static final ZoneId ZONE = ZoneId.of("GMT+1");

    @Override
    public Timestamp currentTime() {
        return TIME;
    }

    @Override
    public ZoneId currentZone() {
        return ZONE;
    }
}
