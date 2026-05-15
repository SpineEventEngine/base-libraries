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
 * A named placeholder that can appear in a [TemplateString].
 *
 * @property name The placeholder name as it appears in a template string (e.g., `field.path`).
 */
public data class Placeholder(public val name: String) {

    /**
     * How this placeholder appears in the "code" of a template string,
     * surrounded by a dollar sign and curly braces (e.g., `${field.path}`).
     */
    public val placed: String
        get() = "\${$name}"

    /**
     * The placeholder [name] wrapped in backticks for use in diagnostic messages.
     */
    public val quoted: String
        get() = "`$name`"

    override fun toString(): String = name

    public companion object {

        /**
         * Matches a template placeholder of the form `${name}`.
         *
         * Group 1 captures the placeholder name — one or more characters
         * between `${` and the next `}`. Any character except `}` is allowed
         * in the name, which permits dotted and underscored identifiers such
         * as `${my.key}` or `${my_key}`.
         */
        internal val regex: Regex = Regex("\\$\\{([^}]+)}")

        /**
         * Extracts all placeholders used within the given [template] string
         * in the order they appear, keeping every occurrence (so a placeholder
         * referenced more than once is returned multiple times).
         */
        public fun extractPlaceholders(template: String): List<Placeholder> =
            regex.findAll(template)
                .map { Placeholder(it.groupValues[1]) }
                .toList()
    }
}
