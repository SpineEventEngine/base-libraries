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

@file:JvmName("Paths")

package io.spine.io

import io.spine.string.toBase64Encoded
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
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
 * Obtains a path with [Unix][Separator.Unix] separators.
 *
 * Unlike the standard library's
 * [invariantSeparatorsPathString][kotlin.io.path.invariantSeparatorsPathString], this function
 * always replaces [Windows][Separator.Windows] separators (`\`) with [Unix][Separator.Unix] ones
 * (`/`), regardless of the current operating system. On a Unix file system both the standard
 * library property and `Path` parsing treat a backslash as an ordinary filename character, leaving
 * a Windows-style path unconverted.
 *
 * @return `this` if the path is already delimited as required, otherwise creates
 *  a new instance with [Windows][Separator.Windows] file separators replaced.
 */
public fun Path.toUnix(): Path =
    if (pathString.contains(Separator.Windows)) {
        Path(pathString.toUnix())
    } else {
        this
    }

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
 * It is intentionally `internal`: it backs [File.toUnixPath] and is not meant to be a
 * general-purpose `String` API.
 */
internal fun String.toUnix(): String = if (contains(Separator.Windows)) {
    replace(Separator.Windows, Separator.Unix)
} else {
    this
}
