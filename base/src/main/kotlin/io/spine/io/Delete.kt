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

@file:JvmName("Delete")

package io.spine.io

import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.nio.file.Path

/**
 * Requests removal of the passed directory when the system shuts down.
 *
 * ### Implementation Note
 *
 * This method creates a new `Thread` for deleting the passed directory.
 * That's why calling it should not be taken lightly. If your application creates
 * several directories that need to be removed when the JVM is terminated, consider
 * gathering them under a common root passed to this method.
 *
 * @see Runtime.addShutdownHook
 */
public fun deleteRecursivelyOnShutdownHook(directory: Path) {
    val runtime = Runtime.getRuntime()
    runtime.addShutdownHook(Thread {
        deleteRecursively(directory) }
    )
}

/**
 * Deletes the passed directory.
 *
 * If the operation fails, the method returns `false`. In such a case,
 * the content of the directory may be partially deleted.
 *
 * @param directory The directory to delete.
 * @return `true` if the directory was successfully deleted, `false` otherwise
 */
@CanIgnoreReturnValue
public fun deleteRecursively(directory: Path): Boolean {
    val success = directory.toFile().deleteRecursively()
    return success
}
