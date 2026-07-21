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
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.base.Time;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.testing.NullPointerTester.Visibility.PACKAGE;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.when_founded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`QueryPredicate` should")
class QueryPredicateTest {

    private static final String FIRST_ISIN = "JP 38998000001";
    private static final String SECOND_ISIN = "JP 38998000002";
    private static final int TEN_STOCKS = 10;
    private static final int HUNDRED_STOCKS = 100;
    private static final Timestamp DEFAULT_TIME = Timestamp.getDefaultInstance();
    private static final Timestamp NOW = Time.currentTime();
    private static final Timestamp IN_BETWEEN = Timestamps.fromSeconds(42);
    private static final boolean IS_TRADED = true;

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .setDefault(QueryPredicate.Builder.class, QueryPredicate.newBuilder(AND))
                .testStaticMethods(QueryPredicate.class, PACKAGE);
    }

    @Test
    @DisplayName("allow copying and joining predicates with some logical operator")
    void allowToCopyAndJoinPredicates() {

        var notTraded =
                firstPredicateOf(queryManufacturer().where(is_traded).is(false));

        var specificIsin =
                firstPredicateOf(queryManufacturer().where(isin).is("A194N"));

        var epoch = Timestamps.fromSeconds(0);
        var foundedAfterEpoch =
                firstPredicateOf(queryManufacturer().where(when_founded).isGreaterThan(epoch));

        var predicatesToJoin = ImmutableList.of(notTraded, specificIsin, foundedAfterEpoch);

        var expectedOperator = OR;
        var joinResult = QueryPredicate.merge(predicatesToJoin, expectedOperator);

        var expectedParameters = predicatesToJoin.stream()
                .flatMap(p -> p.allParams()
                        .stream())
                .collect(toImmutableList());

        assertThat(joinResult).isNotNull();
        assertThat(joinResult.allParams()).containsExactlyElementsIn(expectedParameters);
        assertThat(joinResult.operator()).isEqualTo(expectedOperator);
    }

    @Test
    @DisplayName("when transforming a flat predicate into its DNF, return it as-is")
    void returnFlatPredicateAsIs() {
        var query = queryManufacturer()
                .where(is_traded).is(IS_TRADED)
                .where(when_founded).isLessThan(NOW)
                .build();
        var original = query.subject().predicate();
        var transformed = original.toDnf();
        assertThat(transformed).isEqualTo(original);
    }

    @Test
    @DisplayName("when transforming a predicate that is already in DNF, return it as-is")
    void returnDnfPredicateAsIs() {
        var query = queryManufacturer()
                .either(r -> r.where(stock_count).is(TEN_STOCKS)
                              .where(isin).is(FIRST_ISIN),
                        r -> r.where(stock_count).is(HUNDRED_STOCKS)
                              .where(isin).is(SECOND_ISIN))
                .build();
        var original = query.subject().predicate();
        var transformed = original.toDnf();
        assertThat(transformed).isEqualTo(original);
    }

    /**
     * Tests the transformation of a deeply nested predicate into a predicate in DNF.
     *
     * <p>Source:
     * {@code
     *    A && ((B && C) || (D && E && (F || (G && (H || J)))))
     * }
     *
     * <p>Expected result:
     * {@code
     *    (A && B && C) || (A && D && E && F) || (A && D && E && G && H) || (A && D && E && G && J)
     * }
     *
     * <p>See the actual definitions for {@code A..J} down below in the test code.
     */
    @Test
    @DisplayName("transform the deeply nested predicate into a predicate in DNF")
    void flattenComplexPredicate() {
        Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> firstEither =
                (r) -> r.where(isin).is(FIRST_ISIN)                                         /* B */
                        .where(when_founded).isGreaterThan(IN_BETWEEN);                     /* C */

        Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> childEither1 =
                (r) -> r.where(stock_count).isLessThan(TEN_STOCKS);                         /* F */
        Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> childEither2 =
                (r) -> r.where(stock_count).isGreaterThan(HUNDRED_STOCKS)                   /* G */
                        .either((nested) -> nested.where(when_founded).is(DEFAULT_TIME),    /* H */
                                (nested) -> nested.where(when_founded).isLessThan(NOW));    /* J */

        Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> secondEither =
                (r) -> r.where(isin).is(SECOND_ISIN)                                        /* D */
                        .where(when_founded).isLessThan(IN_BETWEEN)                         /* E */
                        .either(childEither1, childEither2);                                /* G */

        var query = queryManufacturer()
                .where(is_traded).is(IS_TRADED)                                     /* A */
                .either(firstEither, secondEither)
                .build();
        var rootPredicate = query.subject().predicate();
        var dnfPredicate = rootPredicate.toDnf();
        assertThat(dnfPredicate).isNotNull();

        var rootOperator = dnfPredicate.operator();
        assertThat(rootOperator).isEqualTo(OR);

        var allParams = dnfPredicate.allParams();
        assertThat(allParams).isEmpty();

        var children = dnfPredicate.children();
        assertThat(children).hasSize(4);

        assertFirstChild(children);
        assertSecondChild(children);
        assertThirdChild(children);
        assertFourthChild(children);
    }

    /**
     * Expects the first child of the passed collection to be {@code A && B && C}.
     */
    private static void assertFirstChild(ImmutableList<QueryPredicate<Manufacturer>> children) {
        var first = children.get(0);
        assertThat(first.children()).isEmpty();
        assertThat(first.operator()).isEqualTo(AND);

        var allParams = first.allParams();
        assertThat(allParams).hasSize(3);

        var actualA = allParams.get(0);
        assertParamA(actualA);

        var actualB = allParams.get(1);
        assertParam(actualB, isin, EQUALS, FIRST_ISIN);

        var actualC = allParams.get(2);
        assertParam(actualC, when_founded, GREATER_THAN, IN_BETWEEN);
    }

    /**
     * Expects the second child of the passed collection to be {@code A && D && E && F}.
     */
    private static void assertSecondChild(ImmutableList<QueryPredicate<Manufacturer>> children) {
        var second = children.get(1);
        assertThat(second.children()).isEmpty();
        assertThat(second.operator()).isEqualTo(AND);

        var allParams = second.allParams();
        assertThat(allParams).hasSize(4);

        var actualA = allParams.get(0);
        assertParamA(actualA);

        var actualD = allParams.get(1);
        assertParamD(actualD);

        var actualE = allParams.get(2);
        assertParamE(actualE);

        var actualF = allParams.get(3);
        assertParam(actualF, stock_count, LESS_THAN, TEN_STOCKS);
    }

    /**
     * Expects the third child of the passed collection to be {@code A && D && E && G && H}.
     */
    private static void assertThirdChild(ImmutableList<QueryPredicate<Manufacturer>> children) {
        var third = children.get(2);
        assertThat(third.children()).isEmpty();
        assertThat(third.operator()).isEqualTo(AND);

        var allParams = third.allParams();
        assertThat(allParams).hasSize(5);

        var actualA = allParams.get(0);
        assertParamA(actualA);

        var actualD = allParams.get(1);
        assertParamD(actualD);

        var actualE = allParams.get(2);
        assertParamE(actualE);

        var actualG = allParams.get(3);
        assertParamG(actualG);

        var actualH = allParams.get(4);
        assertParam(actualH, when_founded, EQUALS, DEFAULT_TIME);
    }

    /**
     * Expects the fourth child of the passed collection to be {@code A && D && E && G && J}.
     */
    private static void assertFourthChild(ImmutableList<QueryPredicate<Manufacturer>> children) {
        var fourth = children.get(3);
        assertThat(fourth.children()).isEmpty();
        assertThat(fourth.operator()).isEqualTo(AND);

        var allParams = fourth.allParams();
        assertThat(allParams).hasSize(5);

        var actualA = allParams.get(0);
        assertParamA(actualA);

        var actualD = allParams.get(1);
        assertParamD(actualD);

        var actualE = allParams.get(2);
        assertParamE(actualE);

        var actualG = allParams.get(3);
        assertParamG(actualG);

        var actualJ = allParams.get(4);
        assertParam(actualJ, when_founded, LESS_THAN, NOW);
    }

    private static void assertParam(SubjectParameter<?, ?, ?> actual,
                                    RecordColumn<Manufacturer, ?> expectedColumn,
                                    ComparisonOperator expectedOperator, Object expectedValue) {
        assertThat(actual.column().name()).isEqualTo(expectedColumn.name());
        assertThat(actual.operator()).isEqualTo(expectedOperator);
        assertThat(actual.value()).isEqualTo(expectedValue);
    }

    private static void assertParamA(SubjectParameter<?, ?, ?> actualA) {
        assertParam(actualA, is_traded, EQUALS, IS_TRADED);
    }

    private static void assertParamE(SubjectParameter<?, ?, ?> actualE) {
        assertParam(actualE, when_founded, LESS_THAN, IN_BETWEEN);
    }

    private static void assertParamD(SubjectParameter<?, ?, ?> actualD) {
        assertParam(actualD, isin, EQUALS, SECOND_ISIN);
    }

    private static void assertParamG(SubjectParameter<?, ?, ?> actualG) {
        assertParam(actualG, stock_count, GREATER_THAN, HUNDRED_STOCKS);
    }

    private static QueryPredicate<Manufacturer>
    firstPredicateOf(RecordQueryBuilder<ManufacturerId, Manufacturer> query) {
        return query.build()
                    .subject()
                    .predicate();
    }
}
