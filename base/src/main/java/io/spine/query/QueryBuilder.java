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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.base.Field;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A builder for an instance of {@link Query}.
 *
 * @param <I>
 *         the type of identifiers of the records that are queried
 * @param <R>
 *         the type of queried records
 * @param <P>
 *         the type of subject parameters to use when composing the query
 * @param <B>
 *         the type of the {@code QueryBuilder} implementation
 * @param <Q>
 *         the type of {@code Query} implementation
 */
public interface QueryBuilder<I,
                              R extends Message,
                              P extends SubjectParameter<R, ?, ?>,
                              B extends QueryBuilder<I, R, P, B, Q>,
                              Q extends Query<I, R>> {

    /**
     * Creates a new instance of the query on top of this builder.
     */
    Q build();

    /**
     * Returns the type of the queried records.
     */
    Class<R> whichRecordType();

    /**
     * Returns the type of the identifiers for the queried records.
     */
    Class<I> whichIdType();

    /**
     * Returns the criterion for the record identifiers.
     */
    IdParameter<I> whichIds();

    /**
     * Returns the top-level predicate for the record fields.
     */
    QueryPredicate<R> predicate();

    /**
     * Returns the sorting directives to be applied to the resulting dataset.
     */
    ImmutableList<SortBy<?, R>> sorting();

    /**
     * Returns the maximum number of records in the resulting dataset.
     *
     * <p>Returns {@code null} if the limit is not set.
     */
    @Nullable Integer whichLimit();

    /**
     * Returns the field mask to be applied to each of the resulting records.
     *
     * <p>If the mask is not set, returns {@code Optional.empty()}.
     */
    Optional<FieldMask> whichMask();

    /**
     * Adds a predicate to be treated in disjunction with the existing predicates.
     *
     * <p>All expressions passed with every {@code Either} parameter are treated with {@code OR}
     * behavior.
     *
     * <p>Example.
     *
     * <pre>
     *     ProjectView.query()
     *                .either(builder{@literal ->} builder.daysSinceStarted()
     *                                          .isGreaterThan(30),
     *                        builder{@literal ->} builder.status()
     *                                          .is(DONE))
     *                .build();
     * </pre>
     *
     * <p>The {@code ProjectView} query above targets the instances that are either started
     * more than thirty days ago, or those that are in {@code DONE} status.
     *
     * <p>Each {@code Either} is a lambda serving to preserve the current {@code QueryBuilder} with
     * its API and syntax sugar for creating the new predicates, but in a disjunction context.
     *
     * <p>Another example.
     *
     * <pre>
     *    {@literal ImmutableList<Project.Status>} statuses = //...
     *     ProjectView.query()
     *                .either((builder){@literal ->} {
     *                    for (Project.Status status : statuses) {
     *                        builder.status().is(status);
     *                    }
     *                    return builder;
     *                }).build();
     * </pre>
     *
     * <p>This example creates a query for the {@code ProjectView} instances that have one
     * of the expected {@code statuses}. Note that {@code either(..)} is passed with a single
     * argument lambda. Each predicate appended to the builder inside of the passed lambda
     * is treated as a disjunction predicate. Basically, that is just a short form of
     * the expression as follows:
     *
     * <pre>
     *    {@literal ImmutableList<Project.Status>} statuses = //...
     *     ProjectView.query()
     *                // Performs the same as in the previous example. Much less elegant though.
     *                .either(builder{@literal ->} builder.status().is(statuses.get(0)),
     *                        builder{@literal ->} builder.status().is(statuses.get(1)),
     *                        builder{@literal ->} builder.status().is(statuses.get(2)),
     *                        //...
     *                        builder{@literal ->} builder.status().is(statuses.get(lastOne)))
     *                .build();
     * </pre>
     *
     * <p>If several {@code Either} lambdas are passed to the {@code either(..)}, all
     * predicates appended to the builder in them are treated together in an {@code OR} fashion.
     *
     * <p>You may extract lambdas into variables to simplify the code even further:
     *
     * <pre>
     *    {@literal Either<ProjectView.QueryBuilder>} startedMoreThanMonthAgo =
     *                     project{@literal ->} project.daysSinceStarted()
     *                                       .isGreaterThan(daysSinceStarted);
     *    {@literal Either<ProjectView.QueryBuilder>} isDone =
     *                     project{@literal ->} project.status()
     *                                       .is(statusValue);
     *     ProjectView.Query query =
     *             ProjectView.query()
     *                        .either(startedMoreThanMonthAgo, isDone)
     *                        .build();
     * </pre>
     *
     * @return this instance of query builder, for chaining
     */
    @SuppressWarnings("unchecked") // See the implementations on the varargs issue.
    B either(Either<B>... parameters);

    /**
     * Sets the maximum number of records in the resulting dataset.
     *
     * <p>The expected value must be positive.
     *
     * <p>If this method is not called, the limit value remains unset.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B limit(int numberOfRecords);

    /**
     * Sets the field mask to be applied to each of the resulting records.
     *
     * <p>If the mask is not set, the query results contain the records as-is.
     *
     * <p>Any previously set mask values are overridden by this method call.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B withMask(FieldMask mask);

    /**
     * Sets the paths for the field mask to apply to each of the resulting records.
     *
     * <p>If the mask is not set, the query results contain the records as-is.
     *
     * <p>Any previously set mask values are overridden by this method call.
     *
     * @return this instance of query builder, for chaining
     */
    @SuppressWarnings("OverloadedVarargsMethod")    // Each overload has a different parameter type.
    B withMask(String... maskPaths);

    /**
     * Sets the fields to apply as a field mask to each of the resulting records.
     *
     * <p>If the mask is not set, the query results contain the records as-is.
     *
     * <p>Any previously set mask values are overridden by this method call.
     *
     * @return this instance of query builder, for chaining
     */
    @SuppressWarnings("OverloadedVarargsMethod")    // Each overload has a different parameter type.
    B withMask(Field... fields);

    /**
     * Tells to sort the query results in the ascending order of the values in the specified column.
     *
     * <p>Each call to this method adds another sorting directive. Directives are applied one
     * after another, each following determining the order of records remained "equal" after
     * the previous sorting.
     *
     * @param column
     *         the field of the message by which the resulting set should be sorted
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B sortAscendingBy(RecordColumn<R, ?> column);

    /**
     * Tells to sort the query results in the descending order of the values
     * in the specified column.
     *
     * <p>Each call to this method adds another sorting directive. Directives are applied one
     * after another, each following determining the order of records remained "equal" after
     * the previous sorting.
     *
     * @param column
     *         the field of the message by which the resulting set should be sorted
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B sortDescendingBy(RecordColumn<R, ?> column);

    /**
     * Adds a parameter by which the records are to be queried.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    @CanIgnoreReturnValue
    B addParameter(P parameter);

    /**
     * Adds a parameter for the {@link CustomColumn}.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    @CanIgnoreReturnValue
    B addCustomParameter(CustomSubjectParameter<?, ?> parameter);
}
