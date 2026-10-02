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

package io.spine.util;

import com.google.common.collect.Iterators;
import io.spine.annotation.Internal;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Utilities for working with {@link Iterator}s, complementing
 * {@link Iterators com.google.common.collect.Iterators}.
 */
@Internal
public final class Iterators2 {

    /** Prevents instantiation of this utility class. */
    private Iterators2() {
    }

    /**
     * Returns a view of the passed iterator containing the elements matching the predicate.
     *
     * <p>The returned iterator does not support removal.
     *
     * <p>Prefer this method over
     * {@link Iterators#filter(Iterator, com.google.common.base.Predicate) Iterators.filter()}
     * when the elements are not {@code null} — which, in this
     * {@link org.jspecify.annotations.NullMarked @NullMarked} code, is the usual case.
     * Guava declares its method as {@code <T extends @Nullable Object>}, deliberately, so that
     * an iterator over nullable elements can be filtered too. The cost is that the nullness of
     * {@code T} is no longer inferred from the assignment, and assigning the result to an
     * {@code Iterator<@NonNull T>} reads as a nullness mismatch. Declaring {@code <T>} here,
     * in a {@code @NullMarked} package, binds {@code T} to a non-null type and settles it.
     *
     * <p>Takes a {@link Predicate java.util.function.Predicate}, rather than the Guava one
     * the delegate expects, so that the callers do not adapt it themselves.
     *
     * @param unfiltered
     *         the iterator to filter
     * @param retainIfTrue
     *         the predicate matching the elements to retain
     * @param <T>
     *         the type of the iterated elements
     * @return a filtered view of the given iterator
     */
    @SuppressWarnings("NullableProblems")
    public static <T> Iterator<T> filter(Iterator<T> unfiltered,
                                         Predicate<? super T> retainIfTrue) {
        return Iterators.filter(unfiltered, retainIfTrue::test);
    }
}
