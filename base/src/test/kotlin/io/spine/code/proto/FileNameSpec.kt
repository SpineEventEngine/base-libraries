/*
 * Copyright 2022, TeamDev. All rights reserved.
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
package io.spine.code.proto

import com.google.common.testing.NullPointerTester
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.spine.testing.Assertions.assertIllegalArgument
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`FileName` should")
internal class FileNameSpec {

    @Test
    fun `handle 'null's`() {
        NullPointerTester().testStaticMethods(
            FileName::class.java,
            NullPointerTester.Visibility.PACKAGE
        )
    }

    @Test
    fun `require standard extension`() {
        assertIllegalArgument { FileName.of("some_thing") }
    }

    @Test
    fun `return words`() {
        val words = FileName.of("some_file_name.proto").words()

        words.shouldContainExactly("some", "file", "name")
    }

    @Test
    fun `calculate outer class name`() {
        FileName.of("rejections.proto").nameOnlyCamelCase() shouldBe REJECTIONS_FILE_SUFFIX
        FileName.of("many_rejections.proto").nameOnlyCamelCase() shouldBe "ManyRejections"
        FileName.of("many_more_rejections.proto").nameOnlyCamelCase() shouldBe "ManyMoreRejections"
    }

    @Nested internal inner class
    `calculating outer class name` {

        @Test
        fun `one word name`() {
            assertConversion(REJECTIONS_FILE_SUFFIX, "rejections.proto")
        }

        @Test
        fun `two words`() {
            assertConversion("ManyRejections", "many_rejections.proto")
        }

        @Test
        fun `three words`() {
            assertConversion("ManyMoreRejections", "many_more_rejections.proto")
        }

        private fun assertConversion(expected: String, fileName: String) {
            val calculated = FileName.of(fileName)
                .nameOnlyCamelCase()
            calculated shouldBe expected
        }
    }

    @Test
    fun `return file name without extension`() {
        FileName.of("package/commands.proto").nameWithoutExtension() shouldBe "package/commands"
    }

    @Test
    fun `tell commands file kind`() {
        val commandsFile = FileName.of("my_commands.proto")

        commandsFile.isCommands shouldBe true
        commandsFile.isEvents shouldBe false
        commandsFile.isRejections shouldBe false
    }

    @Test
    fun `tell events file kind`() {
        val eventsFile = FileName.of("project_events.proto")

        eventsFile.isEvents shouldBe true
        eventsFile.isCommands shouldBe false
        eventsFile.isRejections shouldBe false
    }

    @Test
    fun `tell rejection file kind`() {
        val rejectionsFile = FileName.of("rejections.proto")

        rejectionsFile.isRejections shouldBe true
        rejectionsFile.isCommands shouldBe false
        rejectionsFile.isEvents shouldBe false
    }

    @Test
    fun `return file name with extension`() {
        val fileName = FileName.of("io/spine/test/test_protos.proto")

        fileName.nameWithExtension() shouldBe "test_protos.proto"
    }

    companion object {
        /** Concatenation is used for avoiding duplicated string warning.  */
        private const val REJECTIONS_FILE_SUFFIX = "Rejection" + 's'
    }
}
