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
