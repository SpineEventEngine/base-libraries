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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
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
     * A common contract for the constructors of {@code AbstractQuery} implementations.
     *
     * <p>Checks that if the limit is set, at least one sorting directive is present as well.
     */
    AbstractQuery(AbstractQueryBuilder<I, R, P, ?, ?> builder) {
        this.subject = new Subject<>(builder);
        this.sorting = checkNotNull(builder.sorting());
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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("subject", subject)
                          .add("sorting", sorting)
                          .add("limit", limit)
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
                Objects.equals(limit, query.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, sorting, limit);
    }
}
