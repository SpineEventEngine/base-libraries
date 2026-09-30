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
