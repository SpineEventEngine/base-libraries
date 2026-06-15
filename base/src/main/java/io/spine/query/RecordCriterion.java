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
     *         the column whose actual value to use later in querying
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
