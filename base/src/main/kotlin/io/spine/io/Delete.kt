/*
 * Copyright 2025, TeamDev. All rights reserved.
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
 * several directories that need to be removed when JVM is terminated, consider
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
