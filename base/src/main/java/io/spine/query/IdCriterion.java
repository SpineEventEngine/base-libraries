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

package io.spine.query;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * An expression that sets the values of record identifiers to be used
 * in a {@linkplain Query query}.
 *
 * <p>Exists in a context of a corresponding
 * {@linkplain AbstractQueryBuilder query builder} instance.
 *
 * @param <I>
 *         the type of identifiers
 * @param <B>
 *         the type of the {@link AbstractQueryBuilder} implementation
 */
public final class IdCriterion<I, B extends AbstractQueryBuilder<I, ?, ?, B, ?>> {

    private final B builder;

    public IdCriterion(B builder) {
        this.builder = builder;
    }

    /**
     * Creates an instance of this criterion with a single passed value to compare to.
     */
    @CanIgnoreReturnValue
    public B is(I value) {
        var parameter = IdParameter.is(value);
        return builder.setIdParameter(parameter);
    }

    /**
     * Creates an instance of this criterion with the passed identifier values.
     */
    @SafeVarargs
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")    /* For convenience. */
    public final B in(I... values) {
        var asSet = ImmutableSet.copyOf(values);
        var parameter = IdParameter.in(asSet);
        return builder.setIdParameter(parameter);
    }

    /**
     * Creates an instance of this criterion with the passed identifier values.
     */
    @CanIgnoreReturnValue
    public final B in(Iterable<I> values) {
        var asList = ImmutableSet.copyOf(values);
        var parameter = IdParameter.in(asList);
        return builder.setIdParameter(parameter);
    }
}
