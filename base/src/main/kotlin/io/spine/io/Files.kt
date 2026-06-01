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

@file:JvmName("Files")

package io.spine.io

import io.spine.string.ensurePrefix
import java.io.File

/**
 * Creates a new instance with the given extension.
 *
 * The function does not check the presence of the file.
 * It does not check if this path represents a directory, either.
 *
 * @param newExtension The new file extension with or without leading `"."`.
 */
public fun File.replaceExtension(newExtension: String): File {
    val newExt = newExtension.ensureDotPrefix()
    return resolveSibling(nameWithoutExtension + newExt)
}

/**
 * Obtains the path with [Unix][Separator.Unix] separators.
 *
 * @return `path` if the file path is already delimited as required, otherwise creates
 *  a new string with the path with [Windows][Separator.Windows] file separators replaced.
 */
public fun File.toUnixPath(): String {
    if (path.contains(Separator.Windows)) {
        return path.toUnix()
    }
    return path
}

/**
 * Ensures that the prefix `.` exists in this string if it is not empty.
 *
 * @return this string if it is already prefixed or is empty,
 *  otherwise returns the new prefixed string.
 */
internal fun String.ensureDotPrefix() =
    if (isEmpty()) {
        this
    } else {
        ensurePrefix(".")
    }
