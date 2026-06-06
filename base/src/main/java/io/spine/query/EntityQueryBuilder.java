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

package io.spine.query;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.base.EntityState;
import io.spine.base.SubscribableField;

import java.util.Arrays;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Base type for builders of queries which are aimed to fetch the records of entity states.
 *
 * @param <I>
 *         the type of entity identifiers
 * @param <S>
 *         the type of an entity state
 * @param <B>
 *         the type of a particular {@code EntityQueryBuilder} implementation
 * @param <Q>
 *         the type of a particular {@link EntityQuery} implementation which this builder
 *         is aimed to build
 */
@SuppressWarnings("AbstractClassNeverImplemented") /* Used in the generated code. */
public abstract class EntityQueryBuilder<I,
                                         S extends EntityState<I>,
                                         B extends EntityQueryBuilder<I, S, B, Q>,
                                         Q extends EntityQuery<I, S, B>>
        extends AbstractQueryBuilder<I, S, EntitySubjectParameter<S, ?>, B, Q> {

    /**
     * Creates an new instance of the {@code EntityQueryBuilder}.
     *
     * @param idType
     *         the type of entity identifiers
     * @param stateType
     *         the type of entity state types
     */
    protected EntityQueryBuilder(Class<I> idType, Class<S> stateType) {
        super(idType, stateType);
    }

    /**
     * Sets the value for the custom column.
     *
     * @param column
     *         the custom column
     * @param value
     *         the value to use in querying
     * @param <V>
     *         the type of the column values
     * @return this instance of builder for chaining
     */
    public final <V> B where(CustomColumn<?, V> column, V value) {
        return column.in(thisRef())
                     .is(value);
    }

    /**
     * Applies the paths of the passed fields as a field mask to each of the resulting records.
     *
     * <p>If the mask is not set, the query results contain the records as-is.
     *
     * <p>Any previously set mask values are overridden by this method call.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    public final B withMask(SubscribableField... fields) {
        var paths = Arrays.stream(fields)
                .map(f -> f.getField()
                           .toString())
                .collect(toImmutableList());
        return withMask(paths);
    }

    /**
     * Builds a query on top of this entity query builder and transforms it according
     * to the logic of the passed transformer.
     *
     * <p>This method is a syntax sugar for a convenient method chaining for those who wishes to use
     * the produced query in their own transformation flow.
     *
     * @param transformer
     *         function transforming the query
     * @param <T>
     *         the type of the resulting object
     * @return a transformed query instance
     */
    public final <T> T build(Function<EntityQuery<?, ?, ?>, T> transformer) {
        checkNotNull(transformer);
        var query = build();
        var result = transformer.apply(query);
        return result;
    }
}
