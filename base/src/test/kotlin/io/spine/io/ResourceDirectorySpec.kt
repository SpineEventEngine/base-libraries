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

package io.spine.io

import com.google.common.testing.EqualsTester
import java.io.File
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Predicate
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`ResourceDirectory` should")
internal class ResourceDirectorySpec {

    private lateinit var directory: ResourceDirectory
    private lateinit var target: Path

    private companion object {
        const val resourceName = "directory"
        val allFiles = listOf(
            ".dot-file",
            "file1.txt",

            "subdir/.dot-file",
            "subdir/file1.txt",
            "subdir/file2.txt",

            "subdir/sub-sub-dir/.dot-file",
            "subdir/sub-sub-dir/file1.txt",
            "subdir/sub-sub-dir/file2.txt",
            "subdir/sub-sub-dir/file3.txt",
        )
        val dotNamed: Predicate<String> = Predicate { s -> s.contains(File.separator + ".dot") }
        val noSubSub: Predicate<String> = Predicate { s -> !s.contains("-sub-") }
    }

    @BeforeEach
    fun obtainDirectory(@TempDir tempDir: Path) {
        directory  = ResourceDirectory.get(resourceName, javaClass.classLoader)
        target = tempDir
    }

    @Test
    fun `copy all content to a target directory`() {
        directory.copyContentTo(target)

        allFiles.forEach { p -> assertExists(p) }
    }

    @Test
    fun `copy content matching a predicate`() {
        val condition = dotNamed.and(noSubSub)
        directory.copyContentTo(target) { path -> condition.test(path.toString()) }

        allFiles
            .filter { name -> condition.test(name) }
            .forEach { p -> assertExists(p) }

        val reverse = dotNamed.negate().and(noSubSub.negate())
        allFiles
            .filter { name -> reverse.test(name) }
            .forEach { p -> assertNotExists(p) }
    }

    @Test
    fun `copy the directory to file system`() {
        directory.copyTo(target)

        assertExists(resourceName)
        allFiles.forEach { p -> assertExists(nestedPath(p)) }
    }

    @Test
    fun `copy the directory with filtering`() {
        val condition = dotNamed.and(noSubSub)
        directory.copyTo(target) { path -> condition.test(path.toString()) }

        assertExists(resourceName)

        allFiles
            .filter { name -> condition.test(name) }
            .forEach { p -> assertExists(nestedPath(p)) }

        val reverse = dotNamed.negate().and(noSubSub.negate())
        allFiles
            .filter { name -> reverse.test(name) }
            .forEach { p -> assertNotExists(nestedPath(p)) }
    }

    @Test
    fun `support equality based on the path`() {
        val same = ResourceDirectory.get(resourceName, javaClass.classLoader)
        val another = ResourceDirectory.get("another-directory", javaClass.classLoader)

        EqualsTester()
            .addEqualityGroup(directory, same)
            .addEqualityGroup(another)
            .testEquals()
    }

    private fun nestedPath(p: String) = Paths.get(resourceName, p).toString()

    private fun assertExists(relativePath: String) {
        val fullPath = target.resolve(relativePath)
        assertTrue(exists(fullPath), "Expected to exist: `${fullPath}`.")
    }

    private fun assertNotExists(relativePath: String) {
        val fullPath = target.resolve(relativePath)
        assertFalse(exists(fullPath), "Expected to NOT exist: `${fullPath}`.")
    }
}
