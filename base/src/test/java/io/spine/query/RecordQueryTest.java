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
import static io.spine.query.given.RecordQueryTestEnv.withSortingAndLimit;

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
            var expected = withSortingAndLimit(almostAsExpected).build();

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
            var expected = withSortingAndLimit(almostAsExpected).build();

            var actual = query.either(disjunctivePredicates(either1(), either2()));
            assertThat(actual).isEqualTo(expected);
        }
    }
}
