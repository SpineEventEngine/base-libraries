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
import io.spine.base.Time;

/**
 * The provider of the current time with a value that does not change.
 */
public class ConstantTimeProvider implements Time.Provider {

    private final Timestamp timestamp;

    public ConstantTimeProvider(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Timestamp currentTime() {
        return timestamp;
    }
}
