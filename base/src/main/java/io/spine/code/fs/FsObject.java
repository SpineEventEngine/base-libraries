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

package io.spine.code.fs;

import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base for source code objects on a file system.
 */
public abstract class FsObject {

    private final Path path;

    protected FsObject(Path path) {
        this.path = checkNotNull(path);
    }

    /**
     * Obtains the path of the file system object.
     */
    public final Path path() {
        return path;
    }

    /**
     * Obtains a parent of this file system object.
     */
    public final @Nullable Path parent() {
        return path.getParent();
    }
    /**
     * Checks if the object is actually present in the file system.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public String toString() {
        return path().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) ||
                ((obj instanceof FsObject other) && Objects.equals(this.path, other.path));
    }
}
