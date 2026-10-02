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
