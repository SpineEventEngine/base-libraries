/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.protobuf.Message;

import java.util.Objects;

/**
 * Defines the sorting order of the {@linkplain Query query} results by the sorting order
 * of values in a particular {@linkplain io.spine.query.RecordColumn column}.
 *
 * @param <C>
 *         type of the column which values are used for sorting
 * @param <R>
 *         the type of the sorted records
 */
public final class SortBy<C extends RecordColumn<R, ?>, R extends Message> {

    private final C column;

    private final Direction direction;

    /**
     * Creates an sorting directive for the given column in a given direction.
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
    @SuppressWarnings("unused") /* Part of the public API. */
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
