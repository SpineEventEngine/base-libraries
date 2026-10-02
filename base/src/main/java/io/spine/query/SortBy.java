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

import com.google.protobuf.Message;

import java.util.Objects;

/**
 * Defines the sorting order of the {@linkplain Query query} results by the sorting order
 * of values in a particular {@linkplain io.spine.query.RecordColumn column}.
 *
 * @param <C>
 *         type of the column whose values are used for sorting
 * @param <R>
 *         the type of the sorted records
 */
public final class SortBy<C extends RecordColumn<R, ?>, R extends Message> {

    private final C column;

    private final Direction direction;

    /**
     * Creates a sorting directive for the given column in a given direction.
     */
    SortBy(C column, Direction direction) {
        this.column = column;
        this.direction = direction;
    }

    /**
     * Returns the column, by which values the query results should be sorted.
     */
    public C column() {
        return column;
    }

    /**
     * Returns the direction in which the column values should be sorted.
     */
    public Direction direction() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SortBy)) {
            return false;
        }
        var by = (SortBy<?, ?>) o;
        return column.equals(by.column) &&
                direction == by.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, direction);
    }
}
