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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.protobuf.util.FieldMaskUtil.fromStringList;
import static io.spine.query.Direction.ASC;
import static io.spine.query.Direction.DESC;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.util.Preconditions2.checkPositive;

/**
 * An abstract base for the builders of {@link AbstractQuery} implementations.
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
abstract class AbstractQueryBuilder<I,
                                    R extends Message,
                                    P extends SubjectParameter<R, ?, ?>,
                                    B extends QueryBuilder<I, R, P, B, Q>,
                                    Q extends Query<I, R>>
        implements QueryBuilder<I, R, P, B, Q> {

    private final Class<I> idType;

    private final Class<R> recordType;

    private IdParameter<I> id = IdParameter.empty();

    private QueryPredicate.Builder<R> rootBuilder = QueryPredicate.newBuilder(AND);

    private QueryPredicate.Builder<R> currentPredicate = rootBuilder;

    private final List<SortBy<?, R>> sorting = new ArrayList<>();

    @MonotonicNonNull
    private Integer limit;

    @MonotonicNonNull
    private FieldMask mask;

    AbstractQueryBuilder(Class<I> idType, Class<R> recordType) {
        this.idType = idType;
        this.recordType = recordType;
    }

    /**
     * Returns this instance of builder.
     */
    protected abstract B thisRef();

    @Override
    public Class<R> whichRecordType() {
        return recordType;
    }

    @Override
    public final Class<I> whichIdType() {
        return idType;
    }

    @Override
    public IdParameter<I> whichIds() {
        return id;
    }

    @Override
    public QueryPredicate<R> predicate() {
        var result = rootBuilder.build();
        return result;
    }

    @Override
    public ImmutableList<SortBy<?, R>> sorting() {
        return ImmutableList.copyOf(sorting);
    }

    @Override
    public @Nullable Integer whichLimit() {
        return limit;
    }

    @Override
    public Optional<FieldMask> whichMask() {
        return Optional.ofNullable(mask);
    }

    @Override
    @CanIgnoreReturnValue
    public final B limit(int numberOfRecords) {
        ensureTopLevel("Limit of Query results");
        checkPositive(numberOfRecords,
                      "Query limit expected to be positive, but got `%s`.", numberOfRecords);
        this.limit = numberOfRecords;
        return thisRef();
    }

    @Override
    @CanIgnoreReturnValue
    public final B withMask(FieldMask mask) {
        ensureTopLevel("Field masks");
        this.mask = checkNotNull(mask);
        return thisRef();
    }

    @Override
    @CanIgnoreReturnValue
    public final B withMask(String ...maskPaths) {
        var pathList = ImmutableList.copyOf(maskPaths);
        return withMask(pathList);
    }

    @Override
    @CanIgnoreReturnValue
    public final B withMask(Field... fields) {
        var paths = Arrays.stream(fields)
                .map(Field::toString)
                .collect(toImmutableList());
        return withMask(paths);
    }

    /**
     * Sets the paths from the passed collection to apply as a field mask
     * to each of the resulting records.
     *
     * <p>This method acts similar to {@link #withMask(String...)}.
     *
     * @return this instance of builder
     * @see #withMask(String...)
     */
    final B withMask(Collection<String> paths) {
        var recordType = whichRecordType();
        var fieldMask = fromStringList(recordType, paths);
        return withMask(fieldMask);
    }

    @Override
    @CanIgnoreReturnValue
    public final B sortAscendingBy(RecordColumn<R, ?> column) {
        checkNotNull(column);
        sorting.add(new SortBy<>(column, ASC));
        return thisRef();
    }

    @Override
    @CanIgnoreReturnValue
    public final B sortDescendingBy(RecordColumn<R, ?> column) {
        checkNotNull(column);
        sorting.add(new SortBy<>(column, DESC));
        return thisRef();
    }

    @Override
    @SafeVarargs
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")    /* For convenience. */
    public final B either(Either<B>... parameters) {
        var asList = Arrays.asList(parameters);
        return either(asList);
    }

    @Internal
    @CanIgnoreReturnValue
    @SuppressWarnings("ReturnValueIgnored")     /* `Either` values applied one by one. */
    public final B either(Iterable<Either<B>> parameters) {
        var previous = currentPredicate;

        var either = QueryPredicate.newBuilder(currentPredicate, OR);
        for (var parameter : parameters) {
            var and = QueryPredicate.newBuilder(either, AND);
            currentPredicate = and;
            parameter.apply(thisRef());

            currentPredicate.build();
        }
        either.build();
        currentPredicate = previous;
        return thisRef();
    }

    /**
     * Adds a parameter by which the records are to be queried.
     *
     * @return this instance of query builder, for chaining
     */
    @Override
    @CanIgnoreReturnValue
    public final B addParameter(P parameter) {
        checkNotNull(parameter);
        currentPredicate.add(parameter);
        return thisRef();
    }

    /**
     * Adds a parameter for the {@link CustomColumn}.
     *
     * @return this instance of query builder, for chaining
     */
    @Override
    @CanIgnoreReturnValue
    public final B addCustomParameter(CustomSubjectParameter<?, ?> parameter) {
        checkNotNull(parameter);
        currentPredicate.addCustom(parameter);
        return thisRef();
    }

    /**
     * Sets the value of the identifier parameter.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    @CanIgnoreReturnValue
    protected final B setIdParameter(IdParameter<I> value) {
        ensureTopLevel("Conditions for IDs");
        id = checkNotNull(value);
        return thisRef();
    }

    /**
     * Replaces the contents of this builder with the contents of the passed predicate.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    @CanIgnoreReturnValue
    protected final B replacePredicate(QueryPredicate<R> value) {
        checkNotNull(value);
        var asBuilder = value.toBuilder();
        return replacePredicate(asBuilder);
    }

    /**
     * Replaces the contents of this builder with the passed predicate builder.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    @CanIgnoreReturnValue
    protected final B replacePredicate(QueryPredicate.Builder<R> value) {
        checkNotNull(value);
        rootBuilder = value;
        currentPredicate = rootBuilder;
        return thisRef();
    }

    /**
     * Adds the sorting directive.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    protected final B addSorting(SortBy<?, R> value) {
        ensureTopLevel("Sorting");
        sorting.add(checkNotNull(value));
        return thisRef();
    }

    private void ensureTopLevel(String action) {
        checkState(currentPredicate.isTopLevel(),
                   "%s may only be set on the top level of a Query DSL-expression.", action);
    }
}
