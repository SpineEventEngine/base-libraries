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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import org.jspecify.annotations.Nullable;

/**
 * A query to fetch the records defined as Protobuf messages.
 *
 * @param <I>
 *         the type of record identifiers
 * @param <R>
 *         the type of records
 */
public interface Query<I, R extends Message> {

    /**
     * Returns the subject of querying.
     */
    Subject<I, R> subject();

    /**
     * Returns the sorting directives to be applied to the query results.
     *
     * <p>In case there are several fields to sort by, the directives are applied one
     * by one starting from the first.
     */
    ImmutableList<SortBy<?, R>> sorting();

    /**
     * Tells the maximum number of records to be returned as a query result.
     *
     * <p>If the limit is set, there must be at least one {@linkplain #sorting() sorting
     * directive} specified.
     *
     * <p>If the limit is not set, returns {@code null}.
     */
    @Nullable Integer limit();

    /**
     * Returns the {@linkplain FieldMask#getDefaultInstance() default instance}
     * of {@code FieldMask}.
     *
     * <p>Formerly, returned the field mask to be applied to each of the resulting records.
     *
     * @deprecated Field masks are no longer supported. The query results always contain
     *         all the fields of the records. Please remove the call.
     */
    @Deprecated
    default FieldMask mask() {
        return FieldMask.getDefaultInstance();
    }
}
