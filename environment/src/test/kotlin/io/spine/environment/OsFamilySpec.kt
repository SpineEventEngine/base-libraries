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

package io.spine.environment

import io.kotest.assertions.throwables.shouldThrow
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
        // Classic (pre-OS X) Mac OS uses the Unix path separator, but is not a Unix.
        "mac os,               ':', false",
        // OpenVMS uses the Unix path separator, but is not a Unix.
        "openvms,              ':', false",
        // Windows is told apart by the path separator alone.
        "windows 11,           ';', false",
    )
    fun `detect Unix, excluding OpenVMS and classic Mac OS`(
        osName: String,
        pathSeparator: String,
        expected: Boolean
    ) {
        OsFamily.Unix.matches(osName, pathSeparator) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "windows 11,           ';', Windows",
        // A Mac is a Unix, too, but `macOS` is the more specific family.
        "mac os x,             ':', macOS",
        "darwin,               ':', macOS",
        "linux,                ':', Unix",
        "sunos,                ':', Unix",
    )
    fun `detect the most specific family`(
        osName: String,
        pathSeparator: String,
        expected: OsFamily
    ) {
        OsFamily.detect(osName, pathSeparator) shouldBe expected
    }

    @Test
    fun `fail to detect an OS which belongs to none of the families`() {
        shouldThrow<IllegalStateException> {
            OsFamily.detect("openvms", ":")
        }
    }

    @Test
    fun `tell the current OS by the system properties`() {
        val osName = System.getProperty("os.name", "").lowercase()
        val pathSeparator = System.getProperty("path.separator", "")

        OsFamily.entries.forEach {
            it.isCurrent() shouldBe it.matches(osName, pathSeparator)
        }
        OsFamily.detect() shouldBe OsFamily.detect(osName, pathSeparator)
    }
}
