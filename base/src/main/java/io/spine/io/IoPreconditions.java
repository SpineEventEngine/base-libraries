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

package io.spine.io;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.nio.file.Files.isDirectory;

/**
 * Preconditions for I/O operations.
 */
public class IoPreconditions {

    private static final String DOES_NOT_EXIST = "The file `%s` does not exist.";

    /** Prevents instantiation of this utility class. */
    private IoPreconditions() {
    }

    /**
     * Ensures that the passed file exists.
     *
     * @return the passed file if it exists
     * @throws IllegalStateException
     *         if the file is missing
     */
    @CanIgnoreReturnValue
    public static File checkExists(File file) throws IllegalStateException {
        checkNotNull(file);
        checkState(file.exists(), DOES_NOT_EXIST, file);
        return file;
    }

    /**
     * Ensures that the file with the passed path exists.
     *
     * @return the passed path if it exists
     * @throws IllegalArgumentException
     *         if the file does not exist
     */
    @CanIgnoreReturnValue
    public static Path checkExists(Path path) throws IllegalArgumentException {
        checkNotNull(path);
        var file = path.toFile();
        checkArgument(file.exists(), DOES_NOT_EXIST, file);
        return path;
    }

    /**
     * Ensures that the passed path is a directory.
     *
     * @return the passed path if it represents a directory
     * @throws IllegalArgumentException
     *         if the path is not a directory
     */
    @CanIgnoreReturnValue
    public static Path checkIsDirectory(Path dir) throws IllegalArgumentException {
        checkNotNull(dir);
        checkArgument(isDirectory(dir), "The path `%s` is not a directory.", dir);
        return dir;
    }

    /**
     * Ensures that the passed {@code File} is not an existing directory.
     */
    @CanIgnoreReturnValue
    public static File checkNotDirectory(File file) {
        if (file.exists() && file.isDirectory()) {
            throw newIllegalArgumentException("File expected, but a directory found: `%s`.",
                                              file.getAbsolutePath());
        }
        return file;
    }
}
