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
 * Unlike the standard library's [invariantSeparatorsPath][kotlin.io.invariantSeparatorsPath],
 * this function always replaces [Windows][Separator.Windows] separators (`\`) with
 * [Unix][Separator.Unix] ones (`/`), regardless of the current operating system.
 * The standard library property replaces only the separator of the current file system, so on
 * Unix-like systems (where the separator is already `/`) it leaves Windows-style
 * backslashes intact.
 *
 * @return `path` if the file path is already delimited as required, otherwise creates
 *  a new string with the path with [Windows][Separator.Windows] file separators replaced.
 */
public fun File.toUnixPath(): String = path.toUnix()

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
