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

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A parameter that defines the expected value for the {@linkplain RecordColumn record column}
 * in scope of a particular {@linkplain Query query}.
 *
 * @param <R>
 *         the type of the queried record
 * @param <C>
 *         the type of the record column
 * @param <V>
 *         type of record column values to which this parameter refers
 */
public abstract class SubjectParameter<R, C extends Column<R, V>, V> {

    private final C column;
    private final V value;
    private final ComparisonOperator operator;

    protected SubjectParameter(C column, ComparisonOperator operator, V value) {
        this.column = checkNotNull(column);
        this.value = checkNotNull(value);
        this.operator = checkNotNull(operator);
    }

    /**
     * Returns the record column that is going to be queried with this parameter.
     */
    public final C column() {
        return column;
    }

    /**
     * Returns the value against which the column should be queried.
     */
    public final V value() {
        return value;
    }

    /**
     * Returns the operator to compare the actual column value with the one
     * set in this {@code SubjectParameter}.
     */
    public final ComparisonOperator operator() {
        return operator;
    }

    @Override
    public String toString() {
        return column.name().value() + ' ' + operator + ' ' + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubjectParameter)) {
            return false;
        }
        var parameter = (SubjectParameter<?, ?, ?>) o;
        return column.equals(parameter.column) &&
                value.equals(parameter.value) &&
                operator == parameter.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, value, operator);
    }
}
