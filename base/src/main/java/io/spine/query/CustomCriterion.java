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

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.ComparisonOperator.EQUALS;

/**
 * Allows specifying the values for the {@link CustomSubjectParameter}s.
 *
 * <p>The custom parameters are set as desired values for the {@link CustomColumn}s.
 *
 * @param <S>
 *         the type of objects that serve as a source for the column values
 * @param <V>
 *         the type of column values
 * @param <B>
 *         the type of query builder in scope of which this criterion exists
 */
final class CustomCriterion<S, V, B extends QueryBuilder<?, ?, ?, B, ?>> {

    private final B builder;
    private final CustomColumn<S, V> column;

    /**
     * Creates a new instance.
     *
     * @param column
     *         the column for which the {@link CustomSubjectParameter} should be set
     * @param builder
     *         the builder in scope of which this criterion exists
     */
    CustomCriterion(CustomColumn<S, V> column, B builder) {
        this.column = column;
        this.builder = builder;
    }

    /**
     * Sets the value that should be equal to the actual column value when querying.
     *
     * <p>Appends the {@code QueryBuilder} associated with this criterion with
     * the {@linkplain CustomSubjectParameter custom subject parameter} based on the specified
     * column and value set by the user.
     *
     * @return the instance of associated query builder
     */
    @CanIgnoreReturnValue
    public B is(V value) {
        checkNotNull(value);
        var param = new CustomSubjectParameter<>(column, value, EQUALS);
        return builder.addCustomParameter(param);
    }
}
