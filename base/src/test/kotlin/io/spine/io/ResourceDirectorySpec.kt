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
