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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An abstract base for queries that may be used to fetch the records defined as Protobuf messages.
 *
 * @param <I>
 *         the type of record identifiers
 * @param <R>
 *         the type of records
 * @param <P>
 *         the type of subject parameters used in a particular implementation
 */
abstract class AbstractQuery<I, R extends Message, P extends SubjectParameter<R, ?, ?>>
        implements Query<I, R> {

    /**
     * Set of criteria defining the subject of querying.
     */
    private final Subject<I, R> subject;

    /**
     * List of sorting directives that define the order of records in the query results.
     *
     * <p>Directives are applied one by one, starting with the first one. The second one
     * and all consecutive directives specify the sorting order of records, which are considered
     * equal by the previous {@code SortBy} directives.
     */
    private final ImmutableList<SortBy<?, R>> sorting;

    /**
     * The maximum number of records in the query results.
     *
     * <p>If not set, all matching records are returned.
     *
     * <p>This field may only be used if at least one {@link SortBy sorting directive} is set.
     */
    private final @Nullable Integer limit;

    /**
     * Defines which fields are returned for the matching records.
     *
     * <p>If not set, the records are returned as-is.
     */
    private final FieldMask mask;

    /**
     * A common contract for the constructors of {@code AbstractQuery} implementations.
     *
     * <p>Checks that if the limit is set, at least one sorting directive is present as well.
     */
    AbstractQuery(AbstractQueryBuilder<I, R, P, ?, ?> builder) {
        this.subject = new Subject<>(builder);
        this.sorting = checkNotNull(builder.sorting());
        this.mask = builder.whichMask().orElse(FieldMask.getDefaultInstance());
        limit = ensureLimit(builder.whichLimit());
    }

    /**
     * Checks that if the limit is set, at least one sorting directive is specified as well.
     *
     * @return the value of query limit, {@code null}-able, as the limit may not be set
     */
    private @Nullable Integer ensureLimit(@Nullable Integer limit) {
        checkState(limit == null || !sorting.isEmpty(),
                      "Query limit must be used with at least one sorting directive set.");
        return limit;
    }

    @Override
    public final Subject<I, R> subject() {
        return subject;
    }

    @Override
    public final ImmutableList<SortBy<?, R>> sorting() {
        return sorting;
    }

    @Override
    public final @Nullable Integer limit() {
        return limit;
    }

    @Override
    public FieldMask mask() {
        return mask;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("subject", subject)
                          .add("sorting", sorting)
                          .add("limit", limit)
                          .add("mask", mask)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractQuery)) {
            return false;
        }
        AbstractQuery<?, ?, ?> query = (AbstractQuery<?, ?, ?>) o;
        return subject.equals(query.subject) &&
                sorting.equals(query.sorting) &&
                Objects.equals(limit, query.limit) &&
                mask.equals(query.mask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, sorting, limit, mask);
    }
}
