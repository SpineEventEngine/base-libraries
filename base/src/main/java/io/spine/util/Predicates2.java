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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with predicates.
 */
public final class Predicates2 {

    /**
     * Prevents instantiation of this utility class.
     */
    private Predicates2() {
    }

    /**
     * Retrieves a {@link Predicate} on a given type {@code T} that distinguishes passed values
     * by some property.
     *
     * <p>The predicate is satisfied (returns {@code true}) iff the result of applying the given
     * {@code selector} function to the predicate argument is not seen before by this function.
     * Therefore, the predicate is stateful and should not be used in parallel streams.
     *
     * @param selector
     *         the key selector function; takes the predicate parameter as an argument and
     *         returns the property to distinct by
     * @param <T>
     *         the type of the values to distinguish
     * @param <K>
     *         the type of the key
     * @return a predicate on {@code T}
     */
    public static <T, K> Predicate<T> distinctBy(Function<T, K> selector) {
        checkNotNull(selector);
        return new DistinctBy<>(selector);
    }

    /**
     * The predicate that remembers previously seen values and returns {@code true}
     * only when a new value is passed.
     *
     * <p>The values are distinguished by properties obtained by a selection function
     * passed to the constructor.
     *
     * <p>The predicate is particularly useful for distinguishing types that do not
     * provide comparison and/or equality functions.
     *
     * @param <T>
     *         the type of values to distinguish
     * @param <K>
     *         the type of property by which distinguish values
     */
    private static final class DistinctBy<T, K> implements Predicate<T> {

        private final Function<T, K> selector;
        private final Set<? super K> seen = new HashSet<>();

        private DistinctBy(Function<T, K> selector) {
            this.selector = selector;
        }

        @Override
        public boolean test(T element) {
            var key = selector.apply(element);
            var isNew = seen.add(key);
            return isNew;
        }
    }
}
