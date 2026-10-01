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

package io.spine.compare

import com.google.auto.service.AutoService
import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import com.google.protobuf.util.Durations
import com.google.protobuf.util.Timestamps

/**
 * Registers comparators provided by the `protobuf-java-util` library
 * for `Timestamp` and `Duration` types.
 */
@AutoService(ComparatorProvider::class)
internal class ProtoComparators : ComparatorProvider {

    override fun registerIn(registry: ComparatorRegistry) = registry.run {
        register<Timestamp>(Timestamps.comparator())
        register<Duration>(Durations.comparator())
    }
}
