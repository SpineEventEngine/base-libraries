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

package io.spine.string

/**
 * Constants for line separators.
 */
public enum class Separator(

    /**
     * The value used in the text to separate lines.
     */
    public val value: String,

    /**
     * The representation of [value] to be used for debugging multi-line strings
     * terminated by [system line separator][nl].
     */
     public val escaped: String
) {

    /**
     * The line separator used by Unix-like systems (including Linux and macOS).
     *
     * This line separator is used by Kotlin string utilities in
     * [String.trimIndent] and [String.replaceIndent].
     */
    LF("\n", "\\n"),

    /**
     * The line separator used by the Classic Mac OS.
     */
    CR("\r", "\\r"),

    /**
     * Windows line separator.
     */
    CRLF("\r\n", "\\r\\n");

    init {
        require(escaped.isNotBlank())
    }

    /**
     * Tells if this separator is used by the current operating system.
     */
    public fun isSystem(): Boolean = value == nl()

    public companion object {

        /**
         * Obtains the system line separator.
         */
        @JvmStatic
        public val system: Separator = findMatching(nl())!!

        /**
         * The shortcut for [System.lineSeparator]. Provided for brevity of the code
         * working with line separators.
         */
        @JvmStatic
        public fun nl(): String = System.lineSeparator()

        /**
         * Obtains line separators that are not used by the current operating system.
         */
        @JvmStatic
        public fun nonSystem(): Iterable<Separator> =
            entries.filter { !it.isSystem() }

        /**
         * Finds a separator that has its value equal to the given string.
         */
        @JvmStatic
        internal fun findMatching(str: String): Separator? =
            entries.find { str == it.value }
    }
}
