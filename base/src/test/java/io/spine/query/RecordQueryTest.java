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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer;
import static io.spine.query.given.RecordQueryTestEnv.conjunctiveBuilder;
import static io.spine.query.given.RecordQueryTestEnv.conjunctivePredicates;
import static io.spine.query.given.RecordQueryTestEnv.disjunctiveBuilder;
import static io.spine.query.given.RecordQueryTestEnv.disjunctivePredicates;
import static io.spine.query.given.RecordQueryTestEnv.either1;
import static io.spine.query.given.RecordQueryTestEnv.either2;
import static io.spine.query.given.RecordQueryTestEnv.either3;
import static io.spine.query.given.RecordQueryTestEnv.either4;
import static io.spine.query.given.RecordQueryTestEnv.moreConjunctivePredicates;
import static io.spine.query.given.RecordQueryTestEnv.withMaskSortingAndLimit;

/**
 * Tests for {@link RecordQuery} behaviour.
 *
 * <p>Most of the features are tested by {@link RecordQueryBuilderTest}, so this test suite
 * only covers those use-cases that aren't related to a {@link RecordQueryBuilder}.
 */
@DisplayName("`RecordQuery` should")
class RecordQueryTest {

    @Nested
    @DisplayName("be extensible by more predicates")
    @SuppressWarnings("unchecked")  /* for simplicity */
    final class Join {

        @Test
        @DisplayName("in conjunction with conjunctive predicates, if this query is disjunctive")
        void disjunctiveAndConjunction() {
            var query = disjunctiveBuilder(either1(), either2()).build();
            var expected =
                    conjunctivePredicates().apply(disjunctiveBuilder(either1(), either2())).build();

            var actual = query.and(conjunctivePredicates());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in conjunction with disjunctive predicates, if this query is disjunctive")
        void disjunctiveAndDisjunction() {
            var query = disjunctiveBuilder(either1(), either2()).build();
            var expected =
                    disjunctiveBuilder(either1(), either2())
                            .either(r -> either3().apply(r),
                                    r -> either4().apply(r))
                            .build();
            var actual = query.and(disjunctivePredicates(either3(), either4()));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in disjunction with disjunctive predicates, if this query is disjunctive")
        void disjunctiveEitherDisjunction() {
            var query = disjunctiveBuilder(either1(), either2()).build();
            var expected = disjunctiveBuilder(either1(), either2(), either3(), either4()).build();

            var actual = query.either(disjunctivePredicates(either3(), either4()));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in disjunction with conjunctive predicates, if this query is disjunctive")
        void disjunctiveEitherConjunction() {
            var query = disjunctiveBuilder(either1(), either2()).build();
            var expected =
                    disjunctiveBuilder(either1(), either2(), r -> conjunctivePredicates().apply(r))
                            .build();
            var actual = query.either(conjunctivePredicates());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in conjunction with conjunctive predicates, if this query is conjunctive")
        void conjunctiveAndConjunction() {
            var query = conjunctiveBuilder().build();
            var expected = conjunctivePredicates().apply(conjunctiveBuilder()).build();

            var actual = query.and(conjunctivePredicates());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in conjunction with disjunctive predicates, if this query is conjunctive")
        void conjunctiveAndDisjunction() {
            var query = conjunctiveBuilder().build();
            var expected = disjunctivePredicates().apply(conjunctiveBuilder()).build();

            var actual = query.and(disjunctivePredicates());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in disjunction with conjunctive predicates, if this query is conjunctive")
        void conjunctiveEitherConjunction() {
            var query = conjunctiveBuilder().build();
            var almostAsExpected = queryManufacturer()
                    .either(
                            r -> conjunctivePredicates().apply(r),
                            r -> moreConjunctivePredicates().apply(r)
                    );
            var expected = withMaskSortingAndLimit(almostAsExpected).build();

            var actual = query.either(moreConjunctivePredicates());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("in disjunction with disjunctive predicates, if this query is conjunctive")
        void conjunctiveEitherDisjunction() {
            var query = conjunctiveBuilder().build();
            var almostAsExpected = queryManufacturer()
                    .either(
                            r -> conjunctivePredicates().apply(r),
                            r -> either1().apply(r),
                            r -> either2().apply(r)
                    );
            var expected = withMaskSortingAndLimit(almostAsExpected).build();

            var actual = query.either(disjunctivePredicates(either1(), either2()));
            assertThat(actual).isEqualTo(expected);
        }
    }
}
