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

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.io.IoPreconditions.checkIsDirectory;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.find;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

/**
 * Utilities for copy operations.
 */
public final class Copy {

    /** Prevents instantiation of this utility class. */
    private Copy() {
    }

    /**
     * Copies a whole directory and its contents into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>The {@code dir} itself is copied as well. For example, if the {@code dir} path is
     * {@code /my/path/to/folder/foo} and the {@code target} path is {@code /my/other/folder}, as
     * a result of this operation, a {@code /my/other/folder/foo} directory will be created and all
     * the contents of the original {@code dir}, including nested directories, will be copied there.
     *
     * @param dir
     *         the directory to copy
     * @param target
     *         the new parent directory
     */
    public static void copyDir(Path dir, Path target) throws IOException {
        copyDir(dir, target, path -> true);
    }

    /**
     * Copies the directory and its contents matching the given predicate into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>The {@code dir} itself is copied as well. For example, if the {@code dir} path is
     * {@code /my/path/to/folder/foo} and the {@code target} path is {@code /my/other/folder}, as
     * a result of this operation, a {@code /my/other/folder/foo} directory will be created and all
     * the contents of the original {@code dir}, including nested directories, will be copied there.
     *
     * @param dir
     *         the directory to copy
     * @param target
     *         the new parent directory
     * @param matching
     *         the predicate accepting the copied content
     */
    public static void copyDir(Path dir, Path target, Predicate<Path> matching) throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);
        doCopy(dir, target, matching, true);
    }

    /**
     * Copies the content of a directory into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>Files under the directory and all nested directories and files under them are copied
     * into the target directory. The directory itself is not copied.
     *
     * @param dir
     *         the directory content of which will be copied
     * @param target
     *         the new parent directory
     */
    public static void copyContent(Path dir, Path target) throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);
        doCopy(dir, target, path -> true, false);
    }

    /**
     * Copies the content of a directory matching the given predicate into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>Files under the directory and all nested directories and files under them are copied
     * into the target directory. The directory itself is not copied.
     *
     * @param dir
     *         the directory content of which will be copied
     * @param target
     *         the new parent directory
     * @param matching
     *         the predicate accepting the copied content
     */
    public static void copyContent(Path dir, Path target, Predicate<Path> matching)
            throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);
        doCopy(dir, target, matching, false);
    }

    private static void doCopy(Path dir,
                               Path target,
                               Predicate<Path> matching,
                               boolean withEnclosingDir) throws IOException {
        var oldParent = withEnclosingDir
                         ? dir.getParent()
                         : dir;
        var paths = contentOf(dir, matching);
        for (var path : paths) {
            var relative = oldParent.relativize(path);
            var newPath = target.resolve(relative);
            if (isDirectory(path)) {
                if (!exists(newPath)) {
                    createDirectories(newPath);
                }
            } else if (isRegularFile(path)) {
                var containingDir = newPath.getParent();
                if (!exists(containingDir)) {
                    createDirectories(containingDir);
                }
                copy(path, newPath);
            }
        }
    }

    /**
     * Obtains all subdirectories and files enclosed the passed directory that match
     * the passed predicate.
     */
    private static ImmutableList<Path> contentOf(Path dir, Predicate<Path> matching)
            throws IOException {
        BiPredicate<Path, BasicFileAttributes> predicate = (path, attrs) -> matching.test(path);
        try (var found = find(dir, Integer.MAX_VALUE, predicate)) {
            var paths = found.collect(toImmutableList());
            return paths;
        }
    }
}
