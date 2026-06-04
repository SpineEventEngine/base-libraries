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

@file:JvmName("Ensure")

package io.spine.io

import com.google.common.io.Files.createParentDirs
import com.google.errorprone.annotations.CanIgnoreReturnValue
import io.spine.io.IoPreconditions.checkNotDirectory
import io.spine.util.Exceptions.newIllegalStateException
import java.io.File
import java.io.IOException
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 * Ensures that the given file exists.
 *
 * Performs no action if the given file [already exists][File.exists].
 *
 * If the given file does not exist, it is created along with its parent directories,
 * if required.
 *
 * If the passed [File] points to the existing directory, an
 * [IllegalArgumentException] is thrown.
 *
 * In case of any I/O issues, the respective exceptions are rethrown as
 * [IllegalStateException].
 *
 * @param file A file to check.
 * @return the given instance.
 * @throws IllegalArgumentException
 *         if the given file is a directory.
 * @throws IllegalStateException
 *         in case of any I/O exceptions.
 */
@CanIgnoreReturnValue
public fun ensureFile(file: File): File {
    checkNotDirectory(file)
    if (file.exists()) {
        return file
    }
    try {
        createParentDirs(file)
        file.createNewFile()
        return file
    } catch (e: IOException) {
        throw IllegalStateException(e)
    }
}

/**
 * Ensures that the file represented by the specified [Path] exists.
 *
 * If the file already exists, no action is performed.
 *
 * If the file does not exist, it is created along with its parent if required.
 *
 * If the specified path represents an existing directory, an
 * [IllegalArgumentException] is thrown.
 *
 * If any I/O errors occur, an [IllegalStateException] is thrown.
 *
 * @param pathToFile The path to the file to check.
 * @return the given instance.
 * @throws IllegalArgumentException
 *         if the given path represents a directory.
 * @throws IllegalStateException
 *         if any I/O errors occur.
 */
@CanIgnoreReturnValue
public fun ensureFile(pathToFile: Path): Path {
    ensureFile(pathToFile.toFile())
    return pathToFile
}

/**
 * Ensures that the specified directory exists, creating it, if it was not done
 * prior to this call.
 *
 * If the given path exists, but refers to a file, the function
 * throws [IllegalStateException].
 *
 * @return the passed instance.
 * @throws IllegalStateException
 *          if the passed path represents an existing file, instead of a directory.
 */
@CanIgnoreReturnValue
public fun ensureDirectory(directory: Path): Path {
    if (!directory.exists()) {
        try {
            createDirectories(directory)
        } catch (e: IOException) {
            throw newIllegalStateException(e, "Unable to create `%s`.", directory)
        }
    } else {
        check(directory.isDirectory()) {
            "The path `$directory` exists, but it is not a directory."
        }
    }
    return directory
}
