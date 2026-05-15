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

import kotlin.collections.iterator

/**
 * Returns a template string with all placeholders substituted with
 * their actual values.
 *
 * For example, for a template string with the following values:
 *
 * ```
 * with_placeholders = "My dog's name is ${dog.name}."
 * placeholder_value = { "dog.name": "Fido" }
 * ```
 *
 * This method will return "My dog's name is Fido."
 */
public fun TemplateString.format(): String {
    checkPlaceholdersHasValue(withPlaceholders, placeholderValueMap) {
        "Cannot format the given `TemplateString`: `$withPlaceholders`. " +
                "Missing value for the following placeholders: `$it`."
    }
    return formatUnsafe()
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
 * For example, for a template string with the following values:
 *
 * ```
 * withPlaceholders = "My dog's name is ${dog.name} and its breed is ${dog.breed}."
 * placeholderValue = { "dog.name": "Fido" }
 * ```
 *
 * This method will return "My dog's name is Fido and its breed is ${dog.breed}.".
 */
public fun TemplateString.formatUnsafe(): String {
    var result = withPlaceholders
    for ((key, value) in placeholderValueMap) {
        result = result.replace("\${$key}", value)
    }
    return result
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
    lazyMessage: (List<String>) -> String =
        { "Missing value for the following template placeholders: `$it`." }
) {
    val neededPlaceholders = extractPlaceholders(template)
    val missing = mutableListOf<String>()
    for (placeholder in neededPlaceholders) {
        if (!placeholders.contains(placeholder)) {
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
    lazyMessage: (List<String>) -> String =
        { "Missing value for the following template placeholders: `$it`." }
): Unit = checkPlaceholdersHasValue(template, placeholders.keys, lazyMessage)

/**
 * Extracts all placeholders used within this [template] string.
 */
public fun extractPlaceholders(template: String): Set<String> =
    PLACEHOLDERS.findAll(template)
        .map { it.groupValues[1] }
        .toSet()

private val PLACEHOLDERS = Regex("\\$\\{([^}]+)}")
