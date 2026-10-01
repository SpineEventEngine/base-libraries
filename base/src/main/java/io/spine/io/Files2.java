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

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Additional utilities for working with files.
 */
public final class Files2 {

    /** Prevents instantiation of this utility class. */
    private Files2() {
    }

    /**
     * Verifies if a passed file exists and has non-zero size.
     */
    public static boolean existsNonEmpty(File file) {
        checkNotNull(file);
        if (!file.exists()) {
            return false;
        }
        var nonEmpty = file.length() > 0;
        return nonEmpty;
    }

    /**
     * Normalizes and transforms the passed path to an absolute file reference.
     */
    public static File toAbsolute(String path) {
        checkNotNull(path);
        var file = new File(path);
        var normalized = file.toPath().normalize();
        var result = normalized.toAbsolutePath().toFile();
        return result;
    }

    /**
     * Obtains the value of the {@code System} property for a temporary directory.
     */
    @SuppressWarnings("AccessOfSystemProperties")
    public static String systemTempDir() {
        return System.getProperty("java.io.tmpdir");
    }
}
