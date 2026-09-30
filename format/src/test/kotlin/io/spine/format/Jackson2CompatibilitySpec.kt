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

package io.spine.format

import com.google.common.collect.ImmutableList
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
import java.util.Optional
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Verifies that files written by Jackson 2.22.1 are still parsed correctly
 * after the migration to Jackson 3.
 *
 * The fixture files under `given/v2` were produced by serializing [v2Instance]
 * through [io.spine.format.write] while the module still
 * depended on `com.fasterxml.jackson:jackson-bom:2.22.1`.
 */
@DisplayName("`Jackson`-backed formats should")
internal class Jackson2CompatibilitySpec {

    @TempDir
    lateinit var tempDir: File

    /**
     * Copies the named fixture into [tempDir], so that [parse] can select
     * the format by the file extension, as production code does.
     */
    private fun fixtureFile(name: String): File {
        val resource = javaClass.getResource("/$FIXTURE_DIR/$name")
        checkNotNull(resource) {
            "Missing test resource: `$FIXTURE_DIR/$name`."
        }
        val target = File(tempDir, name)
        resource.openStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target
    }

    @Test
    fun `parse a JSON file written by Jackson 2`() {
        val file = fixtureFile("user-account.json")
        val parsed = parse<UserAccount>(file)
        parsed shouldBe v2Instance
    }

    @Test
    fun `parse a YAML file written by Jackson 2`() {
        val file = fixtureFile("user-account.yaml")
        val parsed = parse<UserAccount>(file)
        parsed shouldBe v2Instance
    }

    companion object {

        private const val FIXTURE_DIR = "io/spine/format/given/v2"

        /**
         * The value the fixture files were generated from.
         *
         * Deliberately covers the representations that changed between
         * Jackson 2 and 3: a `java.time` type, a Guava collection,
         * and a JDK `Optional`.
         */
        private val v2Instance = UserAccount(
            id = "8f4a2c1e-0000-4000-8000-000000000042",
            creationTimestamp = Instant.parse("2026-08-07T12:30:45.123456789Z"),
            emails = ImmutableList.of(
                EmailAddress("j.doe@example.org"),
                EmailAddress("john@acme-corp.com")
            ),
            gender = Optional.of("X")
        )
    }
}
