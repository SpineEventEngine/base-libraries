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

import static io.spine.query.LogicalOperator.AND;

/**
 * Conjunctive expression.
 *
 * @param <R>
 *         the type of records whose parameters are a part of this expression
 */
final class AndExpression<R> extends Expression<R, AndExpression<R>> {

    private AndExpression(AndBuilder<R> builder) {
        super(AND, builder);
    }

    /**
     * Creates a new instance of builder for this expression.
     *
     * @param <R>
     *         the type of records whose parameters are a part of this expression
     */
    static <R> AndBuilder<R> newBuilder() {
        return new AndBuilder<>();
    }

    /**
     * Attempts to cast the passed expression to {@code AndExpression}.
     */
    @SuppressWarnings("unchecked")
    static <R> AndExpression<R> asAnd(Expression<R, ?> expression) {
        return (AndExpression<R>) expression;
    }

    @Override
    Builder<R, AndExpression<R>, ?> createBuilder() {
        return newBuilder();
    }

    /**
     * Builder of {@code AndExpression}.
     *
     * @param <R>
     *         the type of records, around which the expression is built
     */
    static final class AndBuilder<R> extends Builder<R, AndExpression<R>, AndBuilder<R>> {

        @Override
        AndBuilder<R> thisRef() {
            return this;
        }

        @Override
        AndExpression<R> build() {
            return new AndExpression<>(this);
        }
    }
}
