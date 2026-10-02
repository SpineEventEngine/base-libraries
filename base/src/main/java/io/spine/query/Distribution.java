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

import io.spine.query.AndExpression.AndBuilder;

import java.util.List;

import static io.spine.query.CartesianProducts.cartesianChildren;
import static io.spine.query.CartesianProducts.cartesianCustomParams;
import static io.spine.query.CartesianProducts.cartesianSimpleParams;
import static io.spine.query.LogicalOperator.AND;

/**
 * Utilities helping to apply the Distributive Law of Boolean Algebra to {@link Expression}s.
 */
final class Distribution {

    private Distribution() {
    }

    /**
     * Transforms {@code (ExpressionA) AND (ExpressionB)} into a resulting {@code Expression}
     * by applying the distributive law to the parts of each passed expression.
     *
     * @param <R>
     *         the type of records around which the parts of expressions are built
     * @return a new instance of {@code Expression} equivalent to the conjunction
     *         of the expressions passed
     */
    static <R> Expression<R, ?> conjunctive(Expression<R, ?> first, Expression<R, ?> second) {
        if (first.operator() == AND && second.operator() == AND) {
            return AndExpression.asAnd(first)
                                .concat(AndExpression.asAnd(second));
        }
        if (first.operator() == AND) {
            return distributeCnj(AndExpression.asAnd(first), OrExpression.asOr(second));
        }
        if (second.operator() == AND) {
            return distributeCnj(AndExpression.asAnd(second), OrExpression.asOr(first));
        }
        return distributeCnj(OrExpression.asOr(first), OrExpression.asOr(second));
    }

    /**
     * Applies the distribution law to the given {@code AndExpression} being in conjunction
     * with an {@code OrExpression}.
     */
    private static <R> OrExpression<R> distributeCnj(AndExpression<R> and, OrExpression<R> or) {
        OrExpression.OrBuilder<R> result = OrExpression.newBuilder();
        distributeSimpleParams(and, or.params(), result);
        distributeCustomParams(and, or.customParams(), result);
        distributeChildren(and, or.children(), result);
        return result.build();
    }

    /**
     * Distributes the parts of the passed {@code AND} expression over the list
     * of the disjunctive child {@code Expression}s — as if they are evaluated in conjunction
     * with each other.
     *
     * <p>Input:
     * {@code (AndExpression) && (childA || childB || ...)}
     *
     * <p>Outcome:
     * {@code (AndExpression && childA) || (AndExpression && childB) || ...}
     *
     * <p>Puts the distribution outcome to the {@code Builder} of the target {@code OrExpression}.
     */
    private static <R> void distributeChildren(AndExpression<R> and,
                                               List<Expression<R, ?>> orChildren,
                                               OrExpression.OrBuilder<R> result) {
        for (var child : orChildren) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            and.copyTo(childBuilder);
            if (child.operator() == AND) {
                AndExpression.asAnd(child)
                             .copyTo(childBuilder);
            } else {
                childBuilder.addExpression(child);
            }
            var childAnd = childBuilder.build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Distributes the parts of the passed {@code AND} expression over the list
     * of the disjunctive {@code CustomSubjectParameter}s — as if they are evaluated in conjunction
     * with each other.
     *
     * <p>Input:
     * {@code (AndExpression) && (customParamA || customParamB || ...)}
     *
     * <p>Outcome:
     * {@code (AndExpression && customParamA) || (AndExpression && customParamB) || ...}
     *
     * <p>Puts the distribution outcome to the {@code Builder} of the target {@code OrExpression}.
     */
    private static <R> void
    distributeCustomParams(AndExpression<R> and,
                           List<CustomSubjectParameter<?, ?>> customParams,
                           OrExpression.OrBuilder<R> result) {
        for (var param : customParams) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            and.copyTo(childBuilder);
            childBuilder.addCustomParam(param);
            var childAnd = childBuilder.build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Distributes the parts of the passed {@code AND} expression over the list
     * of the disjunctive {@code SubjectParameter}s — as if they are evaluated in conjunction
     * with each other.
     *
     * <p>Input:
     * {@code (AndExpression) && (paramA || paramB || ...)}
     *
     * <p>Outcome:
     * {@code (AndExpression && paramA) || (AndExpression && paramB) || ...}
     *
     * <p>Puts the distribution outcome to the {@code Builder} of the target {@code OrExpression}.
     */
    private static <R> void distributeSimpleParams(AndExpression<R> and,
                                                   List<SubjectParameter<R, ?, ?>> disjunctiveParams,
                                                   OrExpression.OrBuilder<R> result) {
        for (var param : disjunctiveParams) {
            AndBuilder<R> childBuilder = AndExpression.newBuilder();
            and.copyTo(childBuilder);
            childBuilder.addParam(param);
            var childAnd = childBuilder.build();
            result.addExpression(childAnd);
        }
    }

    /**
     * Distributes the parts of the passed {@code OR} expressions treating them as they
     * are evaluated in conjunction.
     *
     * <p>Given:
     *
     * <p> {@code OrExpressionA == paramA .. || customParamA .. || childA || ..}.
     * <p> {@code OrExpressionB == paramB .. || customParamB .. || childB || ..}.
     *
     * <p>Input:
     *
     * {@code
     * (paramA ..|| customParamA .. || childA || ..)
     * &&
     * (paramB .. || customParamB .. || childB || ..)
     * }
     *
     * <p>The outcome is a cartesian product of parts of each expression. All resulting pairs
     * are expressions such as {@code (paramA && childB)}, joined by conjunction. The resulting
     * {@code OrExpression} contains all these newly created conjunctive pairs as children:
     *
     * <p>Outcome:
     * {@code [(paramA && paramB) || (paramA && customParamB) || (paramA || childB) || ...
     *  (customParamA && paramB) || ...]}
     */
    private static <R>
    OrExpression<R> distributeCnj(OrExpression<R> first, OrExpression<R> second) {
        OrExpression.OrBuilder<R> result = OrExpression.newBuilder();
        List<SubjectParameter<R, ?, ?>> firstParams = first.params();
        List<CustomSubjectParameter<?, ?>> firstCustomParams = first.customParams();
        List<Expression<R, ?>> firstChildren = first.children();

        cartesianSimpleParams(firstParams, second, result);
        cartesianCustomParams(firstCustomParams, second, result);
        cartesianChildren(firstChildren, second, result);

        return result.build();
    }
}
