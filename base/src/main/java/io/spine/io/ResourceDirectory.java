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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.io.Copy.copyContent;
import static io.spine.io.Copy.copyDir;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A directory with resources in the classpath.
 *
 * @see #get(String, ClassLoader)
 * @see Resource#file(String, ClassLoader)
 */
public final class ResourceDirectory extends ResourceObject {

    private ResourceDirectory(String path, ClassLoader classLoader) {
        super(path, classLoader);
    }

    /**
     * Creates a new reference to a resource directory at the context of the given class loader.
     *
     * @param path
     *         the path to the resource directory
     * @param classLoader
     *         the class loader relative to which the resource directory is referenced
     */
    public static ResourceDirectory get(String path, ClassLoader classLoader) {
        checkNotNull(path);
        checkNotNull(classLoader);
        checkNotEmptyOrBlank(path);
        return new ResourceDirectory(path, classLoader);
    }

    /**
     * Obtains the path to this directory under resources.
     */
    public Path toPath() {
        var url = locate();
        try {
            var result = Paths.get(url.toURI());
            return result;
        } catch (URISyntaxException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Copies the content of the directory to the target directory.
     *
     * @param target
     *         the path to existing directory on the file system
     * @see #copyContentTo(Path, Predicate)
     * @see Copy#copyContent(Path, Path)
     */
    public void copyContentTo(Path target) throws IOException {
        checkTarget(target);
        copyContentTo(target, path -> true);
    }

    /**
     * Copies the content of the directory matching the condition to the target directory.
     *
     * @param matching
     *         the condition for accepting the copied content
     * @param target
     *         the path to existing directory on the file system
     * @see #copyContentTo(Path)
     * @see Copy#copyContent(Path, Path, Predicate)
     */
    public void copyContentTo(Path target, Predicate<Path> matching) throws IOException {
        checkTarget(target);
        checkNotNull(matching);
        var from = toPath();
        copyContent(from, target, matching);
    }

    /**
     * Copies this directory to the target directory.
     *
     * @param target
     *         the path to existing directory on the file system
     * @see #copyContentTo(Path, Predicate)
     * @see Copy#copyDir(Path, Path)
     */
    public void copyTo(Path target) throws IOException {
        checkTarget(target);
        copyTo(target, path -> true);
    }

    /**
     * Copies this directory and its content matching the condition to another directory.
     *
     * @param target
     *         the path to existing directory on the file system
     * @see #copyContentTo(Path)
     * @see Copy#copyDir(Path, Path, Predicate)
     */
    public void copyTo(Path target, Predicate<Path> matching) throws IOException {
        checkTarget(target);
        checkNotNull(matching);
        var from = toPath();
        copyDir(from, target, matching);
    }

    private static void checkTarget(Path target) {
        checkNotNull(target);
        checkArgument(Files.exists(target), "The target directory does not exist: `%s`.", target);
    }

    @Override
    public int hashCode() {
        return path().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ResourceDirectory) && super.equals(o);
    }
}
