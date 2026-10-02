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

package io.spine.protobuf

import io.kotest.matchers.shouldBe
import java.time.Duration as JavaDuration
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Durations2` should")
internal class Durations2ConverterSpec {

    @Test
    fun `convert a Java Time duration to a Protobuf duration`() {
        val protoDuration = Durations2.of(JavaDuration.ofSeconds(5))
        protoDuration.seconds shouldBe 5
    }

    @Test
    fun `expose a named converter`() {
        Durations2.converter().toString() shouldBe "Durations2.converter()"
    }
}
