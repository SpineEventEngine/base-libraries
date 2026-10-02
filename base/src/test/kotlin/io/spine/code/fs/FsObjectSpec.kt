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

package io.spine.code.fs

import com.google.common.testing.EqualsTester
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`FsObject` should")
class FsObjectSpec {

    private class StubFsObject(path: Path) : FsObject(path)

    @Test
    fun `expose path`(@TempDir tempDir: Path) {
        val path = tempDir.resolve("some-file")
        val obj = StubFsObject(path)
        obj.path() shouldBe path
    }

    @Test
    fun `expose parent`(@TempDir tempDir: Path) {
        val path = tempDir.resolve("parent/child")
        val obj = StubFsObject(path)
        obj.parent() shouldBe path.parent
    }

    @Test
    fun `tell if exists`(@TempDir tempDir: Path) {
        val path = tempDir.resolve("real-file")
        Files.createFile(path)
        val obj = StubFsObject(path)
        obj.exists() shouldBe true

        val missing = tempDir.resolve("missing-file")
        StubFsObject(missing).exists() shouldBe false
    }

    @Test
    fun `provide 'toString'`() {
        val path = Paths.get("some", "path")
        val obj = StubFsObject(path)
        obj.toString() shouldBe path.toString()
    }

    @Test
    fun `support equality`() {
        val path1 = Paths.get("p1")
        val path2 = Paths.get("p2")
        EqualsTester()
            .addEqualityGroup(StubFsObject(path1), StubFsObject(path1))
            .addEqualityGroup(StubFsObject(path2))
            .testEquals()
    }
}
