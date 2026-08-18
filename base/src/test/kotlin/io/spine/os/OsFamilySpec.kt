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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("`OsFamily` should")
internal class OsFamilySpec {

    @ParameterizedTest
    @CsvSource(
        "windows 11,           ';', true",
        "windows server 2022,  ';', true",
        "linux,                ':', false",
        "mac os x,             ':', false",
        "openvms,              ':', false",
    )
    fun `detect Windows`(osName: String, pathSeparator: String, expected: Boolean) {
        OsFamily.Windows.matches(osName, pathSeparator) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "mac os x,             ':', true",
        "darwin,               ':', true",
        "linux,                ':', false",
        "windows 11,           ';', false",
        "hp-ux,                ':', false",
    )
    fun `detect macOS, including the hosts reporting themselves as Darwin`(
        osName: String,
        pathSeparator: String,
        expected: Boolean
    ) {
        OsFamily.macOS.matches(osName, pathSeparator) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "linux,                ':', true",
        "hp-ux,                ':', true",
        "sunos,                ':', true",
        // A Mac is a Unix, too.
        "mac os x,             ':', true",
        "darwin,               ':', true",
        // OpenVMS uses the Unix path separator, but is not a Unix.
        "openvms,              ':', false",
        // Windows is told apart by the path separator alone.
        "windows 11,           ';', false",
    )
    fun `detect Unix, excluding OpenVMS`(
        osName: String,
        pathSeparator: String,
        expected: Boolean
    ) {
        OsFamily.Unix.matches(osName, pathSeparator) shouldBe expected
    }

    @Test
    fun `tell the current OS by the system properties`() {
        val osName = System.getProperty("os.name", "").lowercase()
        val pathSeparator = System.getProperty("path.separator", "")

        OsFamily.entries.forEach {
            it.isCurrent() shouldBe it.matches(osName, pathSeparator)
        }
    }
}
