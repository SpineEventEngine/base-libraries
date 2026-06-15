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

import com.google.protobuf.Message;
import io.spine.annotation.SPI;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;

/**
 * A query for the records each being a stored Protobuf message.
 *
 * <p>If the queried Protobuf message defines the state of an entity, {@link EntityQuery} serves
 * querying better than this type. See the {@code package-info.java} of this package
 * for more details.
 *
 * @param <I>
 *         the type of the record identifiers
 * @param <R>
 *         the type of the stored records
 * @see EntityQuery
 */
@SPI
public final class RecordQuery<I, R extends Message>
        extends AbstractQuery<I, R, RecordSubjectParameter<R, ?>> {

    private final RecordQueryBuilder<I, R> builder;

    /**
     * Creates a new instance on top of the passed builder.
     */
    RecordQuery(RecordQueryBuilder<I, R> builder) {
        super(builder);
        this.builder = builder;
    }

    /**
     * Creates a builder for this query.
     *
     * @param idType
     *         the type of the identifiers of the records for which the query is built
     * @param recordType
     *         the type of records for which the query is built
     * @param <I>
     *         the type of record identifiers
     * @param <R>
     *         the type of the queried records
     * @return a new instance of {@code RecordQueryBuilder}
     */
    public static <I, R extends Message> RecordQueryBuilder<I, R>
    newBuilder(Class<I> idType, Class<R> recordType) {
        checkNotNull(idType);
        checkNotNull(recordType);
        return new RecordQueryBuilder<>(idType, recordType);
    }

    /**
     * Returns the builder on top of which this query has been created.
     */
    public RecordQueryBuilder<I, R> toBuilder() {
        return builder;
    }

    /**
     * Appends a series of record column predicates to this query treating them in conjunction
     * with those predicates that are already set for querying.
     *
     * <p>This method only processes the column predicates. Additional identifier conditions,
     * field masks, sorting, or limit are ignored.
     */
    public RecordQuery<I, R> and(RecordPredicates<I, R> builder) {
        var result = joinToRootPredicate(builder, AND);
        return result;
    }

    /**
     * Appends a series of records column predicates to this query treating them in disjunction
     * with those predicates that are already set for querying.
     *
     * <p>This method only processes the column predicates. Additional identifier conditions,
     * field masks, sorting, or limit are ignored.
     */
    public RecordQuery<I, R> either(RecordPredicates<I, R> predicates) {
        var result = joinToRootPredicate(predicates, OR);
        return result;
    }

    @SuppressWarnings("ReturnValueIgnored" /* Adjusting builders. */)
    private RecordQuery<I, R> joinToRootPredicate(RecordPredicates<I, R> predicates,
                                                  LogicalOperator operator) {
        var sourcePredicate = subject().predicate();
        var originBuilder = toBuilder();
        if (sourcePredicate.operator() == operator.counterpart()) {
            QueryPredicate.Builder<R> newRoot = QueryPredicate.newBuilder(operator);
            newRoot.addPredicate(sourcePredicate);
            originBuilder.replacePredicate(newRoot);
        }
        if (operator == AND) {
            predicates.apply(originBuilder);
        } else {
            originBuilder.either(predicates::apply);
        }
        var result = originBuilder.build();
        return result;
    }
}
