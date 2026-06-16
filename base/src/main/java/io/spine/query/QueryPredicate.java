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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Joins the {@linkplain SubjectParameter query parameters} with
 * a {@linkplain LogicalOperator logical operator} when querying the records.
 *
 * @param <R>
 *         the type of the queried records
 */
public final class QueryPredicate<R> {

    /**
     * Defines whether the parameters and child predicates are evaluated
     * in conjunction or disjunction with each other.
     */
    private final LogicalOperator operator;

    /**
     * The list of query parameters for the own columns declared by the queried record.
     */
    private final ImmutableList<SubjectParameter<R, ?, ?>> parameters;

    /**
     * The list of query parameters that address the custom columns of the queried record.
     */
    private final ImmutableList<CustomSubjectParameter<?, ?>> customParameters;

    /**
     * The list of child predicates.
     */
    private final ImmutableList<QueryPredicate<R>> children;

    /**
     * Creates a new {@code Predicate}.
     */
    private QueryPredicate(Builder<R> builder) {
        this.operator = checkNotNull(builder.operator);
        checkNotNull(builder.parameters);
        this.parameters = ImmutableList.copyOf(builder.parameters);
        checkNotNull(builder.customParameters);
        this.customParameters = ImmutableList.copyOf(builder.customParameters);
        checkNotNull(builder.children);
        this.children = ImmutableList.copyOf(builder.children);
    }

    /**
     * Creates a new instance of builder for the top-level predicate
     * with the specified logical operator.
     *
     * @param operator
     *         the operator to apply to child elements during predicate evaluation
     * @param <R>
     *         the type of the record that is stored for subject
     */
    static <R> Builder<R> newBuilder(LogicalOperator operator) {
        checkNotNull(operator);
        return new Builder<>(operator);
    }

    /**
     * Creates a new instance of builder for the child query predicate
     * with the specified logical operator and the selected parent.
     *
     * @param parent
     *         the builder of parent predicate
     * @param operator
     *         the operator to apply to child elements during predicate evaluation
     * @param <R>
     *         the type of the record that is stored for subject
     */
    static <R> Builder<R> newBuilder(QueryPredicate.Builder<R> parent, LogicalOperator operator) {
        checkNotNull(parent);
        checkNotNull(operator);
        return new Builder<>(parent, operator);
    }

    /**
     * Returns an operator with which the parameters are joined.
     */
    public LogicalOperator operator() {
        return operator;
    }

    /**
     * Returns the parameters of this predicate that query the values of own columns declared
     * in the queried record.
     */
    public ImmutableList<SubjectParameter<R, ?, ?>> parameters() {
        return parameters;
    }

    /**
     * Returns the parameters of this predicate that relate to the custom columns
     * of the queried record.
     */
    public ImmutableList<CustomSubjectParameter<?, ?>> customParameters() {
        return customParameters;
    }

    /**
     * Returns the list of all parameters of this predicate, including both
     * {@linkplain #parameters() parameters for the own record columns}
     * and those {@linkplain #customParameters() defined for the custom columns}
     * of the queried record.
     */
    public ImmutableList<SubjectParameter<?, ?, ?>> allParams() {
        return ImmutableList.<SubjectParameter<?, ?, ?>>builder()
                .addAll(parameters())
                .addAll(customParameters())
                .build();
    }

    /**
     * Returns the list of child predicates.
     */
    public ImmutableList<QueryPredicate<R>> children() {
        return children;
    }

    /**
     * Converts this predicate along with its children into its disjunctive normal form.
     *
     * <p>This may be required in order to split a complex query several queries, each using only
     * conjunction in its definitions.
     *
     * <p>Therefore if a predicate does not have "parent {@code AND}"-"child {@code OR}" predicate
     * combinations throughout the predicate tree, it is returned as-is.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Disjunctive_normal_form">Disjunctive normal form</a>
     */
    public QueryPredicate<R> toDnf() {
        var result = new TransformToDnf<R>().apply(this);
        return result;
    }

    /**
     * Creates a new {@code Builder} instance from the contents of this predicate.
     */
    public Builder<R> toBuilder() {
        Builder<R> builder = newBuilder(operator);
        copyParams(this, builder);
        copyCustomParams(this, builder);
        copyChildren(this, builder);
        return builder;
    }

    /**
     * Tells whether this predicate has any parameters or children.
     *
     * @return {@code true} if no parameters or children are specified for this predicate,
     *         {@code false} otherwise
     */
    public boolean isEmpty() {
        return children().isEmpty() && allParams().isEmpty();
    }

    private static <R> void copyChildren(QueryPredicate<R> predicate, Builder<R> builder) {
        for (var child : predicate.children) {
            builder.addPredicate(child);
        }
    }

    private static <R> void copyCustomParams(QueryPredicate<R> predicate, Builder<R> builder) {
        for (var customParam : predicate.customParameters) {
            builder.addCustom(customParam);
        }
    }

    private static <R> void copyParams(QueryPredicate<R> predicate, Builder<R> builder) {
        for (var parameter : predicate.parameters) {
            builder.add(parameter);
        }
    }

    /**
     * Merges several predicates into a single predicate.
     *
     * @param predicates
     *         predicates to merge
     * @param operator
     *         operator to use for the merge
     * @param <R>
     *         the type of queried records
     * @return a merge result consisting of parameter-predicate trees of the merged predicates
     */
    public static <R> QueryPredicate<R>
    merge(Iterable<QueryPredicate<R>> predicates, LogicalOperator operator) {
        checkNotNull(predicates);
        checkNotNull(operator);

        Builder<R> builder = newBuilder(operator);
        for (var predicate : predicates) {
            copyParams(predicate, builder);
            copyCustomParams(predicate, builder);
            copyChildren(predicate, builder);
        }
        var result = builder.build();
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("operator", operator)
                          .add("parameters", parameters)
                          .add("customParameters", customParameters)
                          .add("children", children)
                          .toString();
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")     /* That's fine for a predicate tree. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QueryPredicate)) {
            return false;
        }
        var predicate = (QueryPredicate<?>) o;
        return operator == predicate.operator &&
                Objects.equals(parameters, predicate.parameters) &&
                Objects.equals(customParameters, predicate.customParameters) &&
                Objects.equals(children, predicate.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, parameters, customParameters, children);
    }

    /**
     * Builds {@link QueryPredicate} instances.
     *
     * @param <R>
     *         the type of the queried record
     */
    static final class Builder<R> {

        private LogicalOperator operator;
        private final QueryPredicate.@Nullable Builder<R> parent;
        private final List<SubjectParameter<R, ?, ?>> parameters = new ArrayList<>();
        private final List<CustomSubjectParameter<?, ?>> customParameters = new ArrayList<>();
        private final List<QueryPredicate<R>> children = new ArrayList<>();

        /**
         * Creates a new instance of this {@code Builder}.
         *
         * <p>The created builder assumes that the predicate built has no parent
         * (i.e. is a top-level one).
         *
         * @param operator
         *         the operator that defines whether the parameters are evaluated
         *         in conjunction or disjunction with each other
         * @see QueryPredicate#newBuilder(LogicalOperator)
         */
        private Builder(LogicalOperator operator) {
            this.parent = null;
            this.operator = operator;
        }

        /**
         * Creates a new instance of this {@code Builder}.
         *
         * @param parent
         *         the builder of parent predicate
         * @param operator
         *         the operator that defines whether the parameters are evaluated
         *         in conjunction or disjunction with each other
         * @see QueryPredicate#newBuilder(LogicalOperator)
         */
        private Builder(QueryPredicate.Builder<R> parent, LogicalOperator operator) {
            this.parent = parent;
            this.operator = operator;
        }

        /**
         * Returns the logical operator of this builder.
         */
        LogicalOperator operator() {
            return operator;
        }

        /**
         * Adds a parameter for own column declared by the queried records.
         */
        @CanIgnoreReturnValue
        Builder<R> add(SubjectParameter<R, ?, ?> parameter) {
            checkNotNull(parameter);
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Adds multiple parameters for own columns declared by the queried records.
         */
        @CanIgnoreReturnValue
        Builder<R> addParams(Iterable<SubjectParameter<R, ?, ?>> parameters) {
            checkNotNull(parameters);
            for (var parameter : parameters) {
                add(parameter);
            }
            return this;
        }

        /**
         * Adds a parameter, which addresses the {@linkplain CustomColumn custom column}
         * of the queried records.
         */
        @CanIgnoreReturnValue
        Builder<R> addCustom(CustomSubjectParameter<?, ?> parameter) {
            checkNotNull(parameter);
            customParameters.add(parameter);
            return this;
        }

        /**
         * Adds multiple parameters, which address the {@linkplain CustomColumn custom column}s
         * of the queried records.
         */
        @CanIgnoreReturnValue
        Builder<R> addCustomParams(Iterable<CustomSubjectParameter<?, ?>> parameters) {
            checkNotNull(parameters);
            for (var parameter : parameters) {
                addCustom(parameter);
            }
            return this;
        }

        /**
         * Adds a child predicate.
         */
        @CanIgnoreReturnValue
        Builder<R> addPredicate(QueryPredicate<R> child) {
            checkNotNull(child);
            children.add(child);
            return this;
        }

        /**
         * Tells whether the predicate built by this {@code Builder} is a top-level predicate.
         */
        boolean isTopLevel() {
            return parent == null;
        }

        /**
         * Builds a new instance of a predicate based on the data in this builder.
         */
        @CanIgnoreReturnValue
        QueryPredicate<R> build() {
            optimizeForOnlyChild();
            flattenSimilarChildren();
            var result = new QueryPredicate<>(this);
            appendToParent(result);
            return result;
        }

        /**
         * Optimizes the structure of this predicate in a special case.
         *
         * <p>If the following conditions are met:
         *
         * <ul>
         *     <li>the predicate being built is a top-level predicate,
         *
         *     <li>this builder has neither parameters nor custom parameters,
         *
         *     <li>there is only one child predicate,
         * </ul>
         *
         * <p>then the contents of this builder are replaced by the contents of the only child.
         */
        private void optimizeForOnlyChild() {
            if (isTopLevel() && hasNoParams() && children.size() == 1) {
                var onlyChild = children.get(0);
                this.operator = onlyChild.operator();
                addParams(onlyChild.parameters());
                addCustomParams(onlyChild.customParameters());
                children.clear();
                var grandChildren = onlyChild.children;
                for (var grandChild : grandChildren) {
                    addPredicate(grandChild);
                }
            }
        }

        /**
         * Moves the content of each child to this builder, if each child is built around
         * the same logical operator as this builder.
         */
        private void flattenSimilarChildren() {
            var differentChildrenCount = children.stream()
                    .filter(c -> c.operator != operator())
                    .count();
            if (differentChildrenCount == 0) {
                for (var child : children) {
                    copyParams(child, this);
                    copyCustomParams(child, this);
                    copyChildren(child, this);
                }
                children.clear();
            }
        }

        private boolean hasNoParams() {
            return parameters.isEmpty() && customParameters.isEmpty();
        }

        /**
         * Appends this predicate to its parent.
         *
         * <p>In case the predicate has only one part (i.e. a single parameter, just one custom
         * parameter, or a child predicate), only this item is appended to the parent.
         * Otherwise, the whole predicate is appended to the parent one as its child predicates.
         */
        private void appendToParent(QueryPredicate<R> result) {
            if (parent != null) {
                var simplified = false;

                var childrenSize = children.size();
                var paramCount = parameters.size();
                var customCount = customParameters.size();

                if (operator() == parent.operator()) {
                    copyParams(result, parent);
                    copyCustomParams(result, parent);
                    copyChildren(result, parent);
                    simplified = true;
                } else if (childrenSize + paramCount + customCount == 1) {
                    if (paramCount > 0) {
                        parent.add(parameters.get(0));
                    } else if (customCount > 0) {
                        parent.addCustom(customParameters.get(0));
                    } else {
                        parent.addPredicate(children.get(0));
                    }
                    simplified = true;
                }

                if (!simplified) {
                    parent.addPredicate(result);
                }
            }
        }
    }
}
