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

@file:JvmName("Paths")

package io.spine.io

import io.spine.string.toBase64Encoded
import java.io.File
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

/**
 * Converts this path to a Base64-encoded string.
 *
 * @see [String.toBase64Encoded]
 */
public fun Path.toBase64Encoded(): String = toString().toBase64Encoded()

/**
 * Replaces the extension for the file denoted by this path.
 *
 * The function does not check the presence of the file.
 * It does not check if this path represents a directory, either.
 *
 * @param newExtension
 *         a new file extension with or without leading `"."`.
 */
public fun Path.replaceExtension(newExtension: String): Path {
    val newExt = newExtension.ensureDotPrefix()
    return resolveSibling(nameWithoutExtension + newExt)
}

/**
 * Obtains the string representation of this path with [Unix][Separator.Unix] separators.
 *
 * Unlike the standard library's
 * [invariantSeparatorsPathString][kotlin.io.path.invariantSeparatorsPathString], this function
 * always replaces [Windows][Separator.Windows] separators (`\`) with [Unix][Separator.Unix] ones
 * (`/`), regardless of the current operating system. On a Unix file system both the standard
 * library property and `Path` parsing treat a backslash as an ordinary filename character, leaving
 * a Windows-style path unconverted.
 *
 * The conversion deliberately returns a [String] rather than a [Path]. A `Path` cannot carry a
 * non-native separator — its string form always uses the separator of the current file system
 * (`\` on Windows), so wrapping the result back into a `Path` would re-normalize the separators to
 * the host OS and undo the conversion.
 *
 * @return the path string delimited with [Unix][Separator.Unix] separators.
 */
public fun Path.toUnixPath(): String = pathString.toUnix()

/**
 * Provides values of separators used to delimit directories in a file path.
 */
@Suppress("ConstPropertyName") // We use capitalized OS names for constants.
public object Separator {

    /**
     * The separator used by the current OS.
     */
    public val system: Char = File.separatorChar

    /**
     * The separator used in Unix-based systems.
     */
    public const val Unix: Char = '/'

    /**
     * The separator used in the Windows OS family.
     */
    public const val Windows: Char = '\\'
}

/**
 * Replaces Windows path separators (`\`) with those used in Unix-based systems (`/`).
 *
 * The Kotlin standard library already offers
 * [invariantSeparatorsPath][kotlin.io.invariantSeparatorsPath] for [java.io.File] and
 * [invariantSeparatorsPathString][kotlin.io.path.invariantSeparatorsPathString] for
 * [java.nio.file.Path]. They are **not** suitable here because they replace only the separator of
 * the *current* file system: on Unix-like hosts the separator is already `/`, so a Windows-style
 * path keeps its backslashes untouched. The `Path` parser behaves the same way — on a Unix file
 * system a backslash is an ordinary filename character, so `"C:\Windows"` stays a single,
 * unconverted path element.
 *
 * This function always performs the replacement regardless of the host operating system, which is
 * required when processing paths that originate from Windows while running on a non-Windows
 * machine.
 *
 * It backs [Path.toUnixPath] and [File.toUnixPath], and is also available directly for
 * normalizing string paths to the Unix form used in assertions.
 */
public fun String.toUnix(): String = if (contains(Separator.Windows)) {
    replace(Separator.Windows, Separator.Unix)
} else {
    this
}
