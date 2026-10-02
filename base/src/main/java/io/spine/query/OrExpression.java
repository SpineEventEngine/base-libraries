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

import static io.spine.query.LogicalOperator.OR;

/**
 * Disjunctive expression.
 *
 * @param <R>
 *         the type of records whose parameters are a part of this expression
 */
final class OrExpression<R> extends Expression<R, OrExpression<R>> {

    private OrExpression(OrBuilder<R> builder) {
        super(OR, builder);
    }

    /**
     * Creates a new instance of builder for this expression.
     *
     * @param <R>
     *         the type of records whose parameters are a part of this expression
     */
    static <R> OrBuilder<R> newBuilder() {
        return new OrBuilder<>();
    }

    @Override
    Builder<R, OrExpression<R>, ?> createBuilder() {
        return newBuilder();
    }

    /**
     * Treats the passed expression as {@code OrExpression} and transforms it to its builder.
     */
    static <R> OrBuilder<R> asOrBuilder(Expression<R, ?> expression) {
        @SuppressWarnings("unchecked")
        var resultBuilder = (OrBuilder<R>) asOr(expression).toBuilder();
        return resultBuilder;
    }

    /**
     * Attempts to cast the passed expression to {@code OrExpression}.
     */
    @SuppressWarnings("unchecked")
    static <R> OrExpression<R> asOr(Expression<R, ?> expression) {
        return (OrExpression<R>) expression;
    }

    /**
     * Builder of {@code OrExpression}.
     *
     * @param <R>
     *         the type of records, around which the expression is built
     */
    static final class OrBuilder<R> extends Builder<R, OrExpression<R>, OrBuilder<R>> {

        @Override
        OrBuilder<R> thisRef() {
            return this;
        }

        @Override
        OrExpression<R> build() {
            return new OrExpression<>(this);
        }
    }
}
