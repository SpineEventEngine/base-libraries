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

@file:JvmName("TemplateStrings")

package io.spine.string

/**
 * Returns a template string with all placeholders substituted with
 * their actual values.
 *
 * Placeholder values may themselves reference other placeholders using
 * the same `${key}` syntax, and such references are resolved transitively.
 * For example, given `withPlaceholders = "${greeting}"` and
 * `placeholderValue = { "greeting": "Hello, ${name}!", "name": "World" }`,
 * the result is `"Hello, World!"`.
 *
 * For example, for a template string with the following values:
 *
 * ```
 * with_placeholders = "My dog's name is ${dog.name}."
 * placeholder_value = { "dog.name": "Fido" }
 * ```
 *
 * This method will return "My dog's name is Fido."
 *
 * @throws IllegalArgumentException If a placeholder used in the template
 *  (directly or via another placeholder's value) has no corresponding entry
 *  in the placeholder map, or if placeholder references form a cycle.
 */
public fun TemplateString.format(): String {
    checkPlaceholdersHasValue(withPlaceholders, placeholderValueMap) {
        "Cannot format the given `TemplateString`: `$withPlaceholders`. " +
                "Missing value for the following placeholders: ${it.joinQuoted()}."
    }
    return resolveTemplate(withPlaceholders, placeholderValueMap, strict = true)
}

/**
 * Returns a template string with all placeholders substituted with
 * their actual values, without validating that all placeholders have
 * corresponding values.
 *
 * This method does not check whether every placeholder in the template has a matching value
 * in the placeholder map. Any placeholders without a corresponding value will remain
 * unchanged in the resulting string.
 *
 * Placeholder values may reference other placeholders using the same `${key}` syntax;
 * such references are resolved transitively in a deterministic, key-driven order
 * (independent of the underlying map's iteration order). If a chain of references
 * forms a cycle, the offending placeholder is left unresolved as literal `${key}` text.
 *
 * For example, for a template string with the following values:
 *
 * ```
 * withPlaceholders = "My dog's name is ${dog.name} and its breed is ${dog.breed}."
 * placeholderValue = { "dog.name": "Fido" }
 * ```
 *
 * This method will return "My dog's name is Fido and its breed is ${dog.breed}.".
 */
public fun TemplateString.formatUnsafe(): String =
    resolveTemplate(withPlaceholders, placeholderValueMap, strict = false)

/**
 * Substitutes placeholders in [template] using [values], resolving nested references.
 *
 * Resolution is recursive and driven by the placeholders that actually appear in the
 * template (and, transitively, in values), so the result does not depend on the
 * iteration order of [values].
 *
 * @param strict When `true`, missing transitive references and reference cycles
 *  raise [IllegalArgumentException]. When `false`, both are left as literal
 *  `${key}` text in the output.
 */
private fun resolveTemplate(
    template: String,
    values: Map<String, String>,
    strict: Boolean
): String = PLACEHOLDERS.replace(template) { match ->
    resolvePlaceholder(Placeholder(match.groupValues[1]), values, strict, linkedSetOf())
}

private fun resolvePlaceholder(
    placeholder: Placeholder,
    values: Map<String, String>,
    strict: Boolean,
    visiting: LinkedHashSet<Placeholder>
): String {
    if (!values.containsKey(placeholder.name)) {
        if (strict) {
            throw IllegalArgumentException(
                "No value for placeholder ${placeholder.quoted} " +
                        "referenced from a placeholder value."
            )
        }
        return placeholder.placed
    }
    if (!visiting.add(placeholder)) {
        if (strict) {
            val chain = (visiting.dropWhile { it != placeholder } + placeholder)
                .joinToString(" -> ") { it.quoted }
            throw IllegalArgumentException(
                "Cyclic placeholder references detected: $chain."
            )
        }
        return placeholder.placed
    }
    try {
        val value = values.getValue(placeholder.name)
        return PLACEHOLDERS.replace(value) { match ->
            resolvePlaceholder(Placeholder(match.groupValues[1]), values, strict, visiting)
        }
    } finally {
        visiting.remove(placeholder)
    }
}

/**
 * Makes sure that each placeholder within the [template] string is present
 * in the [placeholders] set.
 *
 * @param template The template with placeholders like `${something}`.
 * @param placeholders The set with placeholder names.
 * @param lazyMessage The message to use in [IllegalArgumentException] if the check fails.
 */
public fun checkPlaceholdersHasValue(
    template: String,
    placeholders: Set<String>,
    lazyMessage: (List<Placeholder>) -> String =
        { "Missing value for the following template placeholders: ${it.joinQuoted()}." }
) {
    val neededPlaceholders = extractPlaceholders(template)
    val missing = mutableListOf<Placeholder>()
    for (placeholder in neededPlaceholders) {
        if (!placeholders.contains(placeholder.name)) {
            missing.add(placeholder)
        }
    }
    if (missing.isNotEmpty()) {
        throw IllegalArgumentException(lazyMessage(missing))
    }
}

/**
 * Makes sure that each placeholder within the [template] string has a value
 * in [placeholders] map.
 *
 * @param template The template with placeholders like `${something}`.
 * @param placeholders The map containing placeholders (without curly braces and the dollar sign)
 *  and their values.
 * @param lazyMessage The message to use in [IllegalArgumentException] if the check fails.
 */
public fun checkPlaceholdersHasValue(
    template: String,
    placeholders: Map<String, Any>,
    lazyMessage: (List<Placeholder>) -> String =
        { "Missing value for the following template placeholders: ${it.joinQuoted()}." }
): Unit = checkPlaceholdersHasValue(template, placeholders.keys, lazyMessage)

/**
 * Extracts all placeholders used within this [template] string.
 */
public fun extractPlaceholders(template: String): Set<Placeholder> =
    PLACEHOLDERS.findAll(template)
        .map { Placeholder(it.groupValues[1]) }
        .toSet()

private fun Iterable<Placeholder>.joinQuoted(): String =
    joinToString { it.quoted }

private val PLACEHOLDERS = Regex("\\$\\{([^}]+)}")
