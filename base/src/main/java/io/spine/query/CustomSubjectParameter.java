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

/**
 * A query parameter that defines a condition for a computed value stored along with
 * the queried record.
 *
 * <p>Custom parameters are applied to records storing {@linkplain CustomColumn custom
 * columns} in addition to the columns corresponding to the fields declared in a {@code Message}
 * type of the record. Such custom columns typically store computed values, which for some reason
 * do not belong directly to the record declaration.
 *
 * @param <S>
 *         the type of objects serving as an origin for the parameter values
 * @param <V>
 *         the type of parameter values
 * @see CustomColumn
 * @see Subject
 */
public final class CustomSubjectParameter<S, V> extends SubjectParameter<S, Column<S, V>, V> {

    /**
     * Creates a new instance.
     *
     * @param column
     *         column to use in the query
     * @param value
     *         the value against which the actual column values will be compared when querying
     * @param operator
     *         the comparison operator
     */
    CustomSubjectParameter(CustomColumn<S, V> column, V value, ComparisonOperator operator) {
        super(column, operator, value);
    }
}
