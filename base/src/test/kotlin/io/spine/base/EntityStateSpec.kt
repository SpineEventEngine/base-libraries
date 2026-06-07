/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.base

import io.kotest.matchers.shouldBe
import io.spine.testing.StubMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`EntityState` should")
class EntityStateSpec {

    /**
     * This test simply makes the generic parameter used because the stub class uses it.
     *
     * @see <a href="https://github.com/SpineEventEngine/ProtoData/issues/114">Corresponding issue
     * </a> for mor details.
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
        private const val serialVersionUID: Long = 0L
    }
}

class StubViewState: StubMessage(), ViewState<Long> {
    companion object {
        private const val serialVersionUID: Long = 0L
    }
}
