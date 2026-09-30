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
import com.google.protobuf.Message;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Set of criteria for the records obtained via querying.
 *
 * @param <I>
 *         the type of the identifiers of the queried records
 * @param <R>
 *         the type of the queried records
 */
public final class Subject<I, R extends Message> {

    /**
     * The type of the queried records.
     */
    private final Class<R> recordType;

    /**
     * The type of the identifiers of the queried records.
     */
    private final Class<I> idType;

    /**
     * The criteria put on the identifiers of the records of interest.
     */
    private final IdParameter<I> id;

    /**
     * Predicate, grouping the conditions, against which the actual values
     * of target record fields are compared when querying.
     *
     * <p>The evaluation is done in according to the {@linkplain QueryPredicate#operator()
     * predicate's logical operator}.
     */
    private final QueryPredicate<R> predicate;

    Subject(QueryBuilder<I, R, ?, ?, ?> builder) {
        checkNotNull(builder);
        this.id = checkNotNull(builder.whichIds());
        this.idType = checkNotNull(builder.whichIdType());
        this.recordType = checkNotNull(builder.whichRecordType());
        this.predicate = checkNotNull(builder.predicate());
    }

    /**
     * Returns the type of the queried record.
     */
    public Class<R> recordType() {
        return recordType;
    }

    /**
     * Returns the type of the identifiers of the queried records.
     */
    public Class<I> idType() {
        return idType;
    }

    /**
     * Returns the criteria put on the identifiers of the matched record.
     */
    public IdParameter<I> id() {
        return id;
    }

    /**
     * Returns the predicates for the fields of the matched record.
     */
    public QueryPredicate<R> predicate() {
        return predicate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("recordType", recordType)
                          .add("idType", idType)
                          .add("id", id)
                          .add("predicate", predicate)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subject)) {
            return false;
        }
        var subject = (Subject<?, ?>) o;
        return id.equals(subject.id) &&
                recordType.equals(subject.recordType) &&
                predicate.equals(subject.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recordType, predicate);
    }
}
