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

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.SPI;

/**
 * A column storing the data that is not directly declared as a field in the {@code Message}
 * of an {@link io.spine.base.EntityState EntityState} or a plain record.
 *
 * <p>End-users may choose to store some arbitrary or computed on-the-fly data
 * along with the record. E.g. the time of entity creation or the role of the user created
 * the record etc. That is, something that isn't included into the definition
 * of the {@code Message} type of the record.
 *
 * <p>The framework users would need to provide their own {@link CustomColumn} implementation.
 * When storing objects with custom columns, the values are fetched according
 * to the {@link #valueIn(Object) valueIn(S)} implementation. In it, the {@code S} value represents
 * an arbitrary object serving as a source for the value.
 *
 * @param <S>
 *         the type of objects serving as a source for the column values
 * @param <V>
 *         the type of column values
 * @see CustomSubjectParameter
 * @see QueryPredicate#customParameters()
 */
@SPI
@Immutable
public abstract class CustomColumn<S, V> implements Column<S, V> {

    /**
     * When building a query, creates a criterion for this column.
     *
     * @param builder
     *         a builder of the query
     * @param <B>
     *         the type of the query builder, in scope of which the created criterion will exist
     * @return a new criterion allowing to specify the desired value of this column when querying
     */
    <B extends QueryBuilder<?, ?, ?, B, ?>> CustomCriterion<S, V, B>
    in(B builder) {
        return new CustomCriterion<>(this, builder);
    }
}
