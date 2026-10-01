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
import com.google.protobuf.Message;

/**
 * Sets a condition for a record column to be compared to some value.
 *
 * @param <I>
 *         the type of the record identifiers
 * @param <R>
 *         the type of records
 * @param <V>
 *         the type of the values that the compared record column has
 */
public final class RecordCriterion<I, R extends Message, V>
        extends QueryCriterion<R, V, RecordColumn<R, V>, RecordQueryBuilder<I, R>> {

    /**
     * Creates a new instance.
     *
     * @param column
     *         the column whose actual value is used later in querying
     * @param builder
     *         the builder in scope of which this criterion exists
     */
    RecordCriterion(RecordColumn<R, V> column, RecordQueryBuilder<I, R> builder) {
        super(column, builder);
    }

    @Override
    @CanIgnoreReturnValue
    protected RecordQueryBuilder<I, R>
    addParameter(RecordQueryBuilder<I, R> builder,
                 RecordColumn<R, V> col,
                 ComparisonOperator operator,
                 V value) {
        var parameter = new RecordSubjectParameter<>(col, operator, value);
        return builder.addParameter(parameter);
    }
}
