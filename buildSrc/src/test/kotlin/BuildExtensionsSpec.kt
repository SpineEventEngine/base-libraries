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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Build extensions should")
internal class BuildExtensionsSpec {

    private val project: Project = ProjectBuilder.builder().build()

    @Nested inner class
    `set remote debug options` {

        @Test
        fun `on a JavaExec task once it is created`() {
            project.tasks.register("launch", JavaExec::class.java)

            project.setRemoteDebug("launch")

            val debug = project.tasks.named("launch", JavaExec::class.java).get().debugOptions
            debug.enabled.get() shouldBe true
            debug.port.get() shouldBe BuildSettings.REMOTE_DEBUG_PORT
        }

        @Test
        fun `without creating the task`() {
            var created = false
            project.tasks.register("launch", JavaExec::class.java) { created = true }

            project.setRemoteDebug("launch")

            created shouldBe false
        }

        @Test
        fun `failing at once for a missing task`() {
            shouldThrow<IllegalStateException> {
                project.setRemoteDebug("missing")
            }
        }

        @Test
        fun `failing at once for a task other than JavaExec`() {
            var created = false
            project.tasks.register("plain", DefaultTask::class.java) { created = true }

            shouldThrow<InvalidUserDataException> {
                project.setRemoteDebug("plain")
            }
            created shouldBe false
        }
    }
}
