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
