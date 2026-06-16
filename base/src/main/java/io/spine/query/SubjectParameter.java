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
