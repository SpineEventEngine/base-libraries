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

import io.spine.base.EntityState;

/**
 * A query for the records being the stored Protobuf messages, each declared
 * as a state of an Entity.
 *
 * <p>See the {@code package-info.java} of this package for more details on usage.
 *
 * @param <I>
 *         the type of entity identifiers
 * @param <S>
 *         the type of the entity state
 * @param <B>
 *         the type of a particular {@linkplain EntityQueryBuilder query builder} implementation
 *         to create the query instances
 */
@SuppressWarnings("AbstractClassNeverImplemented") /* Part of the public API. */
public abstract class EntityQuery<I,
                                  S extends EntityState<I>,
                                  B extends EntityQueryBuilder<I, S, B, ?>>
        extends AbstractQuery<I, S, EntitySubjectParameter<S, ?>> {

    private final B builder;

    /**
     * Creates a new query according to the passed query builder.
     */
    protected EntityQuery(B builder) {
        super(builder);
        this.builder = builder;
    }

    /**
     * Returns the builder instance on top of which this query has been created.
     */
    public final B toBuilder() {
        return builder;
    }

    /**
     * Creates a {@link RecordQuery} instance with the same attributes as this entity query.
     */
    public final RecordQuery<I, S> toRecordQuery() {
        var subject = subject();
        var idType = subject.idType();
        var recordType = subject.recordType();
        var destination = RecordQuery.newBuilder(idType, recordType);
        doCopyTo(destination);
        var result = destination.build();
        return result;
    }

    /**
     * Copies the properties of this query to the builder of a similar {@code EntityQuery}.
     */
    public final void copyTo(EntityQueryBuilder<I, S, ?, ?> destination) {
        doCopyTo(destination);
    }

    /**
     * Performs the copying of the properties.
     */
    private void doCopyTo(AbstractQueryBuilder<I, S, ?, ?, ?> destination) {
        copyIdParameter(destination);
        copyPredicate(destination);
        copySorting(destination);
        copyLimit(destination);
        copyMask(destination);
    }

    /**
     * Copies the ID parameter value from the current instance to the destination builder.
     */
    private void copyIdParameter(AbstractQueryBuilder<I, S, ?, ?, ?> destination) {
        destination.setIdParameter(subject().id());
    }

    /**
     * Copies the top-level predicate from the current query instance to the destination builder.
     */
    private void copyPredicate(AbstractQueryBuilder<I, S, ?, ?, ?> destination) {
        var predicate = subject().predicate();
        destination.replacePredicate(predicate);
    }

    /**
     * Copies the field mask value from the current instance to the destination builder.
     */
    private void copyMask(AbstractQueryBuilder<I, S, ?, ?, ?> destination) {
        var sourceMask = mask();
        if (sourceMask != null) {
            destination.withMask(sourceMask);
        }
    }

    /**
     * Copies the limit value from the current instance to the destination builder.
     */
    private void copyLimit(AbstractQueryBuilder<I, S, ?, ?, ?> destination) {
        var sourceLimit = limit();
        if (sourceLimit != null) {
            destination.limit(sourceLimit);
        }
    }

    /**
     * Copies the sorting directives from the current instance to the destination builder.
     */
    private void copySorting(AbstractQueryBuilder<I, S, ?, ?, ?> destination) {
        for (var sourceSortBy : sorting()) {
            destination.addSorting(sourceSortBy);
        }
    }
}
