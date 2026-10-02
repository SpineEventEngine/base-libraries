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

package io.spine.io

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * A [GLOB](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob) pattern for
 * matching file paths.
 *
 * @see java.nio.file.FileSystem.getPathMatcher
 */
public data class Glob(val pattern: String) {

    init {
        require(pattern.isNotEmpty())
    }

    private val matcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")

    /**
     * Checks if the given path matches this pattern.
     */
    public fun matches(path: Path): Boolean = matcher.matches(path)

    public companion object {

        /**
         * A pattern that matches any file.
         */
        @JvmField
        public val any: Glob = Glob("**")

        /**
         * Creates a pattern that matches any file with the given extensions.
         *
         * @param extensions File extensions with or without the leading dot.
         *   If no extensions are specified, the created pattern will match
         *   files without extensions.
         * @see [extensionLowerAndUpper]
         */
        @JvmStatic
        public fun extension(vararg extensions: String): Glob =
            create(extensions.toList(), false)

        /**
         * Creates a pattern that matches any file with the given extensions.
         *
         * @param extensions File extensions with or without the leading dot.
         *  If no extensions are specified, the created pattern will match
         *  files without extensions.
         * @see [extensionLowerAndUpper]
         */
        @JvmStatic
        public fun extension(extensions: Iterable<String>): Glob =
            create(extensions, false)

        /**
         * Creates a pattern that matches any file with the given extensions in lower- and
         * uppercase versions of specified file extensions.
         *
         * Even if char sequences are in passed the `mIxeD` case, only `lower`- and `UPPER`- case
         * versions of the sequences will be used.
         *
         * @see [extension]
         */
        @JvmStatic
        public fun extensionLowerAndUpper(vararg extensions: String): Glob =
            create(extensions.toList(), true)

        /**
         * Creates a pattern that matches any file with the given extensions in lower- and
         * uppercase versions of specified file extensions.
         *
         * Even if char sequences are in passed the `mIxeD` case, only `lower`- and `UPPER`- case
         * versions of the sequences will be used.
         *
         * @see [extension]
         */
        @JvmStatic
        public fun extensionLowerAndUpper(extensions: Iterable<String>): Glob =
            create(extensions, true)

        private fun create(extensions: Iterable<String>, allowUpperCase: Boolean): Glob {
            val ext: List<String> = extensions.withCaseOptions(allowUpperCase)
            if (ext.isEmpty()) {
                return Glob("**.")
            }
            return if (ext.size > 1) {
                val commaSeparated = ext.joinToString(",")
                Glob("**.{$commaSeparated}")
            } else {
                Glob("**.${ext[0]}")
            }
        }
    }
}

/**
 * Transforms this iteration of char sequences into a sorted list of values that do not
 * have a leading dot.
 *
 * @param allowUpperCase If `true`, each entry of the returned list would have
 *  the lower- and uppercase version of the sequence.
 *  Otherwise, the sequences would be used as is.
 */
private fun Iterable<String>.withCaseOptions(allowUpperCase: Boolean): List<String> {
    if (!iterator().hasNext()) {
        return listOf()
    }
    val values = mutableSetOf<String>()
    val result = mutableListOf<String>()
    for (seq in this) {
        val dotless = seq.withoutLeadingDot()
        if (allowUpperCase) {
            values.add(dotless.lowercase())
            values.add(dotless.uppercase())
        } else {
            values.add(dotless)
        }
    }
    result.addAll(values)
    result.sort()
    return result
}

/**
 * Obtains the string without the leading dot, if present.
 */
private fun String.withoutLeadingDot(): String {
    if (isEmpty()) return this
    return if (this[0] == '.') substring(1)
    else this
}
