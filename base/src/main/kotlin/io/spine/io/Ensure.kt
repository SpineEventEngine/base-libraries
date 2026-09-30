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
 * If the passed [File] points to an existing directory, an
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
