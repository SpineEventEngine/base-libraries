/*
 * Copyright 2025, TeamDev. All rights reserved.
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
 * of a project, and is loaded by a specified {@link ClassLoader} on runtime.
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
