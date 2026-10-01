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

import com.google.common.base.Suppliers;
import io.spine.annotation.Internal;

import java.util.function.Supplier;

/**
 * Utilities for working with {@link Supplier}s, complementing
 * {@link Suppliers com.google.common.base.Suppliers}.
 */
@Internal
public final class Suppliers2 {

    /** Prevents instantiation of this utility class. */
    private Suppliers2() {
    }

    /**
     * Returns a supplier that obtains the value from the given delegate on the first call,
     * and returns that same value on the calls that follow.
     *
     * <p>Prefer this method over
     * {@link Suppliers#memoize(com.google.common.base.Supplier) Suppliers.memoize()}
     * when the supplied value is not {@code null} — which, in this
     * {@link org.jspecify.annotations.NullMarked @NullMarked} code, is the usual case.
     * Guava declares its method as {@code <T extends @Nullable Object>}, deliberately, so that
     * a {@code null} value can be memoized too. The cost is that the nullness of {@code T} is
     * no longer inferred from the assignment, and assigning the result to a
     * {@code Supplier<@NonNull T>} reads as a nullness mismatch. Declaring {@code <T>} here,
     * in a {@code @NullMarked} package, binds {@code T} to a non-null type and settles it.
     *
     * @param delegate
     *         the supplier of the value to memoize
     * @param <T>
     *         the type of the supplied value
     * @return a supplier that memoizes the value of the delegate
     */
    @SuppressWarnings("NullableProblems")
    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return Suppliers.memoize(delegate::get);
    }
}
