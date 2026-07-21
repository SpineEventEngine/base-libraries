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

@file:JvmName("CharSequences")

package io.spine.string

/**
 * Tells if this sequence contains the given separator.
 */
public fun CharSequence.contains(s: Separator): Boolean =
    contains(s.value)

/**
 * Tells if this sequence contains any of the given characters.
 */
public fun CharSequence.containsAny(vararg char: Char): Boolean =
    char.any {
        contains(it)
    }

/**
 * Obtains all line separators found in this sequence.
 *
 * Keys of the returned map are indexes of the characters the separators occupy in this sequence.
 * Values are the separators themselves.
 */
public fun CharSequence.findLineSeparators(): Map<IntRange, Separator> {
    val allSeparators = Regex("\\R")
    val separators = allSeparators.findAll(this)
    val matchingSeparators = separators.mapNotNull { match ->
        Separator.findMatching(match.value)?.let { Pair(match.range, it) }
    }
    return matchingSeparators.toMap()
}

/**
 * Tells if this char sequence contains at least one line separator.
 */
public fun CharSequence.containsLineSeparators(): Boolean =
    !findLineSeparators().values.isEmpty()

/**
 * Tells if this char sequence contains at least one non-system line separator.
 */
public fun CharSequence.containsNonSystemLineSeparator(): Boolean {
    val found = findLineSeparators().values.any { !it.isSystem() }
    return found
}

/**
 * Finds all the line separators in this sequence and replaces them with escaped replacements
 * like "\r" or "\n", so that the separators become visible in logging or other
 * diagnostic output.
 *
 * @see CharSequence.revealLineSeparators
 */
public fun CharSequence.escapeLineSeparators(): String {
    val replacementFn: (s: Separator) -> String = Separator::escaped
    return doReplace(replacementFn)
}

/**
 * Finds all the line separators in this sequence and replaces them with escaped
 * replacements\ like "\r" or "\n" followed by the system line separator, so that
 * the separators become visible in logging or other diagnostic output.
 *
 * @see CharSequence.escapeLineSeparators
 */
public fun CharSequence.revealLineSeparators(): String {
    val replacementFn: (s: Separator) -> String = { it.escaped + Separator.nl() }
    return doReplace(replacementFn)
}

/**
 * Replaces line separators in this sequence taking the replacement text as the result
 * of the given function on a [Separator].
 *
 * If there are no separators in this sequence, returns [this]. Otherwise, the sequence is
 * copied in the chunks from separator to separator, replacing the separators with the values
 * obtained from [replacementFn].
 */
private fun CharSequence.doReplace(replacementFn: (s: Separator) -> String): String {
    val separators = findLineSeparators()
    val s = toString()
    if (separators.isEmpty()) {
        return s
    }
    return buildString {
        var prevEntry: Map.Entry<IntRange, Separator>? = null
        separators.forEach { entry ->
            val range = entry.key
            val separator = entry.value
            if (prevEntry == null) {
                append(s.substring(0, range.first))
            } else {
                append(s.substring(prevEntry.key.last + 1, range.first))
            }
            val replacement = replacementFn.invoke(separator)
            append(replacement)
            prevEntry = entry
        }
        val lastSeparatorEnd = prevEntry!!.key.last + 1
        if (lastSeparatorEnd < s.length) {
            append(s.substring(lastSeparatorEnd))
        }
    }
}
