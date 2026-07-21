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
     * Returns the field mask to be applied to each of the resulting records.
     *
     * <p>If the mask is not set, returns a {@linkplain FieldMask#getDefaultInstance()
     * default instance} of the {@code FieldMask}.
     */
    FieldMask mask();
}
