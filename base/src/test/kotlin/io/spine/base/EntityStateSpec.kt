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

package io.spine.base

import io.kotest.matchers.shouldBe
import io.spine.testing.StubMessage
import java.io.Serial
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`EntityState` should")
class EntityStateSpec {

    /**
     * This test simply makes the generic parameter used because the stub class uses it.
     *
     * @see <a href="https://github.com/SpineEventEngine/ProtoData/issues/114">Corresponding issue
     * </a> for more details.
     */
    @Test
    fun `generic parameter for ID`() {
        EntityState::class.java.isAssignableFrom(StubEntityState::class.java) shouldBe true
    }

    /**
     * Ensures [ViewState] remains a usable synonym of [ProjectionState].
     *
     * The alias has no consumers in this repository; this test keeps it exercised
     * so it is not flagged as unused while it stays a part of the public API.
     */
    @Test
    fun `expose 'ViewState' as an alias of 'ProjectionState'`() {
        ProjectionState::class.java.isAssignableFrom(StubViewState::class.java) shouldBe true
        EntityState::class.java.isAssignableFrom(StubViewState::class.java) shouldBe true
    }
}

class StubEntityState: StubMessage(), EntityState<Long> {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 0L
    }
}

class StubViewState: StubMessage(), ViewState<Long> {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 0L
    }
}
