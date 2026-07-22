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
import io.spine.query.AndExpression.AndBuilder;
import io.spine.query.OrExpression.OrBuilder;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.LogicalOperator.AND;

/**
 * Helper utility performing the multiplication of parts of boolean expressions that result
 * in cartesian products.
 */
final class CartesianProducts {

    /**
     * Disables the instantiation of this helper.
     */
    private CartesianProducts() {
    }

    /**
     * Multiplies each of the passed simple parameters onto each of the parts
     * of the passed {@code OrExpression}, handling the multiplication
     * as {@code A && (B || C || D ...) <=> (A && B) || (A && C) || (A && D) || ...)}
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    static <R>
    void cartesianSimpleParams(List<SubjectParameter<R, ?, ?>> simpleParams,
                               OrExpression<R> expression,
                               OrBuilder<R> result) {
        checkNotNull(simpleParams);
        checkNotNull(expression);
        checkNotNull(result);
        for (var givenParam : simpleParams) {
            paramOverSimpleParams(givenParam, expression.params(), result);
            paramOverCustomParams(givenParam, expression.customParams(), result);
            paramOverChildren(givenParam, expression.children(), result);
        }
    }

    /**
     * Multiplies each of the passed child expressions onto each of the parts
     * of the passed {@code OrExpression}, handling the multiplication
     * as {@code A && (B || C || D ...) <=> (A && B) || (A && C) || (A && D) || ...)}
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    static <R>
    void cartesianChildren(List<Expression<R, ?>> children,
                           OrExpression<R> expression,
                           OrBuilder<R> result) {
        checkNotNull(children);
        checkNotNull(expression);
        checkNotNull(result);
        for (var firstChild : children) {
            childOverSimpleParams(firstChild, expression.params(), result);
            childOverCustomParams(firstChild, expression.customParams(), result);
            childOverChildren(firstChild, expression.children(), result);
        }
    }

    /**
     * Multiplies each of the passed custom parameters onto each of the parts
     * of the passed {@code OrExpression}, handling the multiplication
     * as {@code A && (B || C || D ...) <=> (A && B) || (A && C) || (A && D) || ...)}
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    static <R>
    void cartesianCustomParams(List<CustomSubjectParameter<?, ?>> customParams,
                               OrExpression<R> expression,
                               OrBuilder<R> result) {
        checkNotNull(customParams);
        checkNotNull(expression);
        checkNotNull(result);
        for (var customParam : customParams) {
            customOverSimple(customParam, expression, result);
            customOverCustom(customParam, expression, result);
            customOverChildren(customParam, expression, result);
        }
    }

    /**
     * Multiplies the given simple parameter over the simple parameters
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R>
    void paramOverSimpleParams(SubjectParameter<R, ?, ?> param,
                               ImmutableList<SubjectParameter<R, ?, ?>> simpleParams,
                               OrBuilder<R> result) {
        for (var simpleParam : simpleParams) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            var childAnd = childBuilder.addParam(param)
                                       .addParam(simpleParam)
                                       .build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given simple parameter over the custom parameters
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R>
    void paramOverCustomParams(SubjectParameter<R, ?, ?> param,
                               ImmutableList<CustomSubjectParameter<?, ?>> customParams,
                               OrBuilder<R> result) {
        for (var customParam : customParams) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            var childAnd = childBuilder.addParam(param)
                                       .addCustomParam(customParam)
                                       .build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given simple parameter over the child expressions
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R> void paramOverChildren(SubjectParameter<R, ?, ?> param,
                                              ImmutableList<Expression<R, ?>> children,
                                              OrBuilder<R> result) {
        for (var child : children) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            addChild(child, childBuilder);
            var childAnd = childBuilder.addParam(param).build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given custom parameter over the child expressions
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R> void customOverChildren(CustomSubjectParameter<?, ?> customParam,
                                               OrExpression<R> expression,
                                               OrBuilder<R> result) {
        for (var secondChild : expression.children()) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            addChild(secondChild, childBuilder);
            var childAnd = childBuilder.addCustomParam(customParam).build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given custom parameter over the custom parameters
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R> void customOverCustom(CustomSubjectParameter<?, ?> customParam,
                                             OrExpression<R> second,
                                             OrBuilder<R> result) {
        for (var secondCustom : second.customParams()) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            var childAnd = childBuilder.addCustomParam(customParam)
                                       .addCustomParam(secondCustom)
                                       .build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given custom parameter over the simple parameters
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R> void customOverSimple(CustomSubjectParameter<?, ?> customParam,
                                             OrExpression<R> expression,
                                             OrBuilder<R> result) {
        for (var secondParam : expression.params()) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            var childAnd = childBuilder.addCustomParam(customParam)
                                       .addParam(secondParam)
                                       .build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given child expression over the simple parameters
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R>
    void childOverSimpleParams(Expression<R, ?> child,
                               ImmutableList<SubjectParameter<R, ?, ?>> simpleParams,
                               OrBuilder<R> result) {
        for (var simpleParam : simpleParams) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            addChild(child, childBuilder);
            var childAnd = childBuilder.addParam(simpleParam).build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given child expression over the custom parameters
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R>
    void childOverCustomParams(Expression<R, ?> child,
                               ImmutableList<CustomSubjectParameter<?, ?>> customParams,
                               OrBuilder<R> result) {
        for (var customParam : customParams) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            addChild(child, childBuilder);
            var childAnd = childBuilder.addCustomParam(customParam)
                                       .build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Multiplies the given child expression over the child expressions
     * of the passed {@code OrExpression}.
     *
     * <p>Appends the resulting {@code AndExpression}s to the provided result builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R> void childOverChildren(Expression<R, ?> child,
                                              ImmutableList<Expression<R, ?>> children,
                                              OrBuilder<R> result) {
        for (var orChild : children) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            addChild(child, childBuilder);
            addChild(orChild, childBuilder);
            var childAnd = childBuilder.build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Appends a child expression to the passed {@code AndBuilder}.
     *
     * <p>If the passed child expression is a conjunctive one, its contents are copied
     * to the contents of the passed builder. And in the other case, it is appended as a child
     * of the passed builder.
     *
     * @param <R>
     *         the type of records that query conditions are described by the processed expressions
     */
    private static <R> void addChild(Expression<R, ?> child, AndBuilder<R> destination) {
        if (child.operator() == AND) {
            AndExpression.asAnd(child)
                         .copyTo(destination);
        } else {
            destination.addExpression(child);
        }
    }
}
