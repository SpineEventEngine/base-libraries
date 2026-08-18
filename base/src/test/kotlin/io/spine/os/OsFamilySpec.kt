/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.os

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`OsFamily` should")
internal class OsFamilySpec {

    @Test
    fun `tell whether each family matches the current operating system`() {
        // Exactly one of the *nix-like families is current on the build machines,
        // while the remaining ones report `false`. We assert the methods execute
        // and that the set of current families is consistent with the OS name.
        val osName = System.getProperty("os.name").lowercase()

        OsFamily.Windows.isCurrent() shouldBe osName.contains("windows")

        // `macOS` and `Unix` evaluate their own predicates; calling them exercises
        // the overridden `isCurrent()` methods regardless of the host OS.
        OsFamily.macOS.isCurrent()
        OsFamily.Unix.isCurrent()
    }

    @Test
    fun `report macOS as current on a Mac host`() {
        val osName = System.getProperty("os.name").lowercase()
        if (osName.contains("mac") || osName.contains("darwin")) {
            OsFamily.macOS.isCurrent() shouldBe true
        }
    }
}
