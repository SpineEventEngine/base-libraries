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
public final class IoPreconditions {

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
