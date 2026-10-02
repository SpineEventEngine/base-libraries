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
