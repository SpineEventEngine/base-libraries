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
