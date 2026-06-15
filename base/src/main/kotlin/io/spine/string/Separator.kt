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
