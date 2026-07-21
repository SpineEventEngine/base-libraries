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
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Logical expression suitable for transformations.
 *
 * <p>Each expression is equivalent to a {@link QueryPredicate}. However, as long as predicates
 * are a part of the public API, the visibility of their entry points must be more restrictive.
 * It makes it difficult to use them in operations like applying the distributive law
 * of boolean algebra.
 *
 * <p>Therefore, this type and its descendants serve as "staging" data structures for performing
 * manipulations with the expression parts.
 *
 * @param <R>
 *         the type of records, which parameters are a part of this expression
 * @param <E>
 *         the type of concrete implementation of a logical expression
 */
abstract class Expression<R, E extends Expression<R, E>> {

    private final LogicalOperator operator;

    private final ImmutableList<SubjectParameter<R, ?, ?>> params;
    private final ImmutableList<CustomSubjectParameter<?, ?>> customParams;
    private final ImmutableList<Expression<R, ?>> children;

    /**
     * Creates an expression around the passed logical operator and the parts specified
     * in the {@code Builder}.
     */
    Expression(LogicalOperator operator, Builder<R, E, ?> builder) {
        this.operator = operator;
        this.params = ImmutableList.copyOf(builder.params);
        this.customParams = ImmutableList.copyOf(builder.customParams);
        this.children = ImmutableList.copyOf(builder.children);
    }

    /**
     * Returns the logical operator of this expression.
     */
    final LogicalOperator operator() {
        return operator;
    }

    /**
     * Returns the simple parameters of this expression.
     */
    final ImmutableList<SubjectParameter<R, ?, ?>> params() {
        return params;
    }

    /**
     * Returns the custom parameters of this expression.
     */
    final ImmutableList<CustomSubjectParameter<?, ?>> customParams() {
        return customParams;
    }

    /**
     * Returns the child expressions of this expression.
     */
    final ImmutableList<Expression<R, ?>> children() {
        return children;
    }

    /**
     * Returns a copy of this expression, but without child expressions.
     */
    final E withoutChildren() {
        var builder = createBuilder();
        copyParams(builder);
        copyCustomParams(builder);
        return builder.build();
    }

    /**
     * Tells if this expression has child expressions.
     */
    final boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Tells whether this expression has neither parameters nor children.
     */
    final boolean isEmpty() {
        var result =
                params().isEmpty()
                        && customParams().isEmpty()
                        && children().isEmpty();
        return result;
    }

    /**
     * Creates a new builder of the same {@code Expression} type as the current one.
     */
    abstract Builder<R, E, ?> createBuilder();

    /**
     * Creates a new expression by concatenating the parts of this instance with the parts
     * of the passed expression.
     */
    E concat(E another) {
        var result = createBuilder();
        copyTo(result);
        another.copyTo(result);
        return result.build();
    }

    /**
     * Copies the parts of this expression to the passed {@code Builder}.
     */
    void copyTo(Builder<R, E, ?> result) {
        copyParams(result);
        copyCustomParams(result);
        copyChildren(result);
    }

    private void copyChildren(Builder<R, E, ?> result) {
        result.addExpressions(children());
    }

    private void copyCustomParams(Builder<R, E, ?> result) {
        result.addCustomParams(customParams());
    }

    private void copyParams(Builder<R, E, ?> result) {
        result.addParams(params());
    }

    /**
     * Creates a new {@code Builder} with the same content as this expression.
     */
    Builder<R, ?, ?> toBuilder() {
        var result = createBuilder();
        copyTo(result);
        return result;
    }

    /**
     * An abstract base for builders of {@code Expression} descendants.
     *
     * @param <R>
     *         the type of records, which parameters are a part of this expression built
     * @param <E>
     *         the type of the expression built
     * @param <B>
     *         the type of particular {@code Builder} implementation
     */
    abstract static class Builder<R, E extends Expression<R, E>, B extends Builder<R, E, B>> {

        private final List<SubjectParameter<R, ?, ?>> params = new ArrayList<>();
        private final List<CustomSubjectParameter<?, ?>> customParams = new ArrayList<>();
        private final List<Expression<R, ?>> children = new ArrayList<>();

        /**
         * Returns the current instance of {@code Builder}.
         */
        abstract B thisRef();

        /**
         * Builds the expression from the contents of this {@code Builder}.
         */
        abstract E build();

        /**
         * Adds a simple parameter to this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B addParam(SubjectParameter<R, ?, ?> parameter) {
            checkNotNull(parameter);
            params.add(parameter);
            return thisRef();
        }

        /**
         * Adds multiple simple parameters to this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B addParams(Iterable<SubjectParameter<R, ?, ?>> parameters) {
            checkNotNull(parameters);
            for (var param : parameters) {
                addParam(param);
            }
            return thisRef();
        }

        /**
         * Adds a custom parameter to this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B addCustomParam(CustomSubjectParameter<?, ?> parameter) {
            checkNotNull(parameter);
            customParams.add(parameter);
            return thisRef();
        }

        /**
         * Adds multiple custom parameters to this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B addCustomParams(Iterable<CustomSubjectParameter<?, ?>> parameters) {
            checkNotNull(parameters);
            for (var param : parameters) {
                addCustomParam(param);
            }
            return thisRef();
        }

        /**
         * Adds a child expression to this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B addExpression(Expression<R, ?> expression) {
            checkNotNull(expression);
            children.add(expression);
            return thisRef();
        }

        /**
         * Adds multiple child expression to this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B addExpressions(Collection<? extends Expression<R, ?>> children) {
            checkNotNull(children);
            for (Expression<R, ?> child : children) {
                addExpression(child);
            }
            return thisRef();
        }

        /**
         * Removes all child expressions from this {@code Builder}.
         */
        @CanIgnoreReturnValue
        B clearChildren() {
            children.clear();
            return thisRef();
        }
    }
}
