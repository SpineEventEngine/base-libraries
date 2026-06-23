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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;

/**
 * A restriction to a value of a record column applied when querying the records.
 *
 * @param <R>
 *         the type of the record
 * @param <V>
 *         the type of the values of the record column
 * @param <C>
 *         the type of the column to which this restriction is put
 * @param <B>
 *         the type of the query builder, in scope of which this restiction is set
 */
abstract class QueryCriterion<R extends Message,
                              V,
                              C extends RecordColumn<R, V>,
                              B extends AbstractQueryBuilder<?, ?, ?, B, ?>> {

    private final C column;
    private final B builder;

    /**
     * Creates a new instance.
     *
     * @param column
     *         the column whose actual value is used later in querying
     * @param builder
     *         the builder of an query in scope of which the criterion is created
     */
    QueryCriterion(C column, B builder) {
        this.column = column;
        this.builder = builder;
    }

    /**
     * Adds a parameter for comparing the values of the column with the passed value
     * using the passed operator.
     *
     * @param col
     *         the record column queried
     * @param operator
     *         the comparison operator
     * @param value
     *         the value to which actual column values will be compared
     * @return a new instance of the subject parameter
     */
    @CanIgnoreReturnValue
    protected abstract B addParameter(B builder, C col, ComparisonOperator operator, V value);

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column equals the one provided.
     *
     * <p>The shorter {@link #is(Object) is()} alias has the same effect and reads more
     * fluently from Java. Prefer this method in Kotlin, where {@code is} is a reserved
     * word and calling {@code is()} requires wrapping the name in backticks.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     * @see #is(Object)
     */
    @CanIgnoreReturnValue
    public B isEqualTo(V value) {
        checkNotNull(value);
        return addParameter(builder, column, EQUALS, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column equals to the one provided.
     *
     * <p>This method is a short form of {@link #isEqualTo(Object) isEqualTo()}.
     * It is a convenience wrapper for working with a {@code QueryCriterion} from Java.
     *
     * <p>In Kotlin, prefer {@link #isEqualTo(Object) isEqualTo()} because {@code is} is
     * a reserved word there, so calling this method requires wrapping its name in
     * backticks.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     * @see #isEqualTo(Object)
     */
    @CanIgnoreReturnValue
    public B is(V value) {
        return isEqualTo(value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is less than the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    @CanIgnoreReturnValue
    public B isLessThan(V value) {
        checkNotNull(value);
        return addParameter(builder, column, LESS_THAN, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is less or equal to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    @CanIgnoreReturnValue
    public B isLessOrEqualTo(V value) {
        checkNotNull(value);
        return addParameter(builder, column, LESS_OR_EQUALS, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is greater than the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    @CanIgnoreReturnValue
    public B isGreaterThan(V value) {
        checkNotNull(value);
        return addParameter(builder, column, GREATER_THAN, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is greater or equal to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    @CanIgnoreReturnValue
    public B isGreaterOrEqualTo(V value) {
        checkNotNull(value);
        return addParameter(builder, column, GREATER_OR_EQUALS, value);
    }
}
