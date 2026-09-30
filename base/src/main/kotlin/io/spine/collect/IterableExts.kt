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

@file:JvmName("MoreIterables")

package io.spine.collect

import com.google.common.collect.Iterables

/**
 * Obtains the only element in the receiver `Iterable`.
 *
 * @throws NoSuchElementException if the iterable is empty.
 * @throws IllegalArgumentException if the iterable contains multiple elements.
 */
public fun <E> Iterable<E>.theOnly(): E = Iterables.getOnlyElement(this)

/**
 * Builds a `Sequence` that consists of the elements of this `Iterable` and
 * the given [infix] between them.
 *
 * Example:
 *  - `listOf(0, 1, 2).interlaced(42)` -> `[0, 42, 1, 42, 2]`;
 *  - `listOf("sea", "Moon", "Earth", "Sun").interlaced("of")` ->
 *    `["sea", "of", "Moon", "of", "Earth", "of", "Sun"]`;
 *  - `listOf<String>().interlaced("")` -> `[]`.
 */
public fun <T> Iterable<T>.interlaced(infix: T): Sequence<T> = sequence {
    forEachIndexed { index, element ->
        if (index != 0) {
            yield(infix)
        }
        yield(element)
    }
}
