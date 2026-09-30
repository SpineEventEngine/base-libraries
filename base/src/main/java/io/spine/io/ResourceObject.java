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

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * Abstract base for objects stored in program resources.
 *
 * <p>Such a resource is represented by a string path relative to the {@code "resources"} directory
 * of a project, and is loaded by a specified {@link ClassLoader} at runtime.
 */
abstract class ResourceObject {

    private final String path;
    private final ClassLoader classLoader;

    /**
     * Creates a new resource with the given path and classloader.
     */
    ResourceObject(String path, ClassLoader classLoader) {
        this.path = checkNotNull(path);
        this.classLoader = checkNotNull(classLoader);
    }

    private @Nullable URL findUrl() {
        var url = classLoader.getResource(path);
        return url;
    }

    /**
     * Checks if the resource with such a name exists in the classpath.
     *
     * @return {@code true} if the resource is present, {@code false} otherwise
     */
    public boolean exists() {
        var resource = findUrl();
        return resource != null;
    }

    /**
     * Obtains a {@link URL} of the resolved resource.
     *
     * @return the resource URL
     * @throws IllegalStateException if the resource cannot be resolved
     *         (i.e., the file does not exist)
     */
    public URL locate() {
        var url = findUrl();
        if (url == null) {
            throw cannotFind();
        }
        return url;
    }

    /** Obtains the resource path of this resource object as passed on creation. */
    final String path() {
        return path;
    }

    /**
     * Creates an exception stating that the resource cannot be found.
     */
    final IllegalStateException cannotFind() {
        return newIllegalStateException("Unable to find %s.", this);
    }

    /**
     * Enumerates all resources with the given path.
     */
    final Enumeration<URL> resources() throws IOException {
        return classLoader.getResources(path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceObject other)) {
            return false;
        }
        return path.equals(other.path);
    }

    @Override
    public String toString() {
        return format("`%s` via `ClassLoader` `%s`", path, classLoader);
    }
}
