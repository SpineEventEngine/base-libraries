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
