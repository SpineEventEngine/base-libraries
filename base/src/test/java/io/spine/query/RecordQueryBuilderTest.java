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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.query.given.RecordQueryBuilderTestEnv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.query.Direction.ASC;
import static io.spine.query.Direction.DESC;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.when_founded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.assertHasParamValue;
import static io.spine.query.given.RecordQueryBuilderTestEnv.generateIds;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerId;
import static io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer;
import static io.spine.query.given.RecordQueryBuilderTestEnv.subjectWithNoParameters;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`RecordQueryBuilder` should")
class RecordQueryBuilderTest {

    /**
     * The Epoch Thursday.
     */
    private static final Timestamp THURSDAY = Timestamps.fromSeconds(0);

    @Nested
    @DisplayName("create `RecordQuery` instances")
    final class CreateQuery {

        @Test
        @DisplayName("with no parameters")
        void empty() {
            var actual = queryManufacturer().build();
            var subject = subjectWithNoParameters(actual);
            assertThat(subject.id()
                              .values()).isEmpty();
            RecordQueryBuilderTestEnv.assertNoSortingAndLimit(actual);
        }

        @Test
        @DisplayName("which hold the type of the queried record and the type of its ID")
        void withRecordType() {
            var query = queryManufacturer().build();
            var subject = query.subject();
            assertThat(subject.recordType()).isEqualTo(Manufacturer.class);
            assertThat(subject.idType()).isEqualTo(ManufacturerId.class);
        }

        @Test
        @DisplayName("by a single identifier value")
        void byId() {
            var expectedId = manufacturerId();
            var query = queryManufacturer()
                    .id().is(expectedId)
                    .build();
            var subject = subjectWithNoParameters(query);

            var actualIdParam = subject.id();
            assertThat(actualIdParam.values()).containsExactly(expectedId);
        }

        @Test
        @DisplayName("by several identifier values")
        void bySeveralIds() {
            var expectedValues = generateIds(24);
            var query = queryManufacturer()
                    .id().in(expectedValues)
                    .build();
            var subject = subjectWithNoParameters(query);

            var actualIdParam = subject.id();
            assertThat(actualIdParam.values()).isEqualTo(expectedValues);
        }

        @Test
        @DisplayName("by the values of several columns")
        void byColumnValues() {
            var stocksAreTraded = true;
            var isinValue = "JP 3633400001";
            var query = queryManufacturer()
                    .where(isin).is(isinValue)
                    .where(when_founded).isLessOrEqualTo(THURSDAY)
                    .where(is_traded).is(stocksAreTraded)
                    .build();

            var rootPredicate = query.subject().predicate();
            assertThat(rootPredicate.operator()).isEqualTo(AND);
            assertThat(rootPredicate.children()).isEmpty();
            assertThat(rootPredicate.customParameters()).isEmpty();

            var params = rootPredicate.parameters();
            assertThat(params).hasSize(3);
            assertHasParamValue(params, isin, EQUALS, isinValue);
            assertHasParamValue(params, when_founded, LESS_OR_EQUALS, THURSDAY);
            assertHasParamValue(params, is_traded, EQUALS, stocksAreTraded);

        }

        @Test
        @DisplayName("by the value of either of the columns")
        void byEitherColumn() {
            var isinValue = "JP 3899800001";
            var stocksAreTraded = true;
            var query = queryManufacturer()
                    .where(when_founded).isLessThan(THURSDAY)
                    .either((r) -> r.where(isin).is(isinValue),
                            (r) -> r.where(is_traded).is(stocksAreTraded))
                    .build();
            var rootPredicate = query.subject().predicate();
            assertThat(rootPredicate.operator()).isEqualTo(AND);

            var parameters =rootPredicate.parameters();
            assertThat(parameters.size()).isEqualTo(1);
            assertHasParamValue(parameters, when_founded, LESS_THAN, THURSDAY);

            var children = rootPredicate.children();
            assertThat(children.size()).isEqualTo(1);
            var either = children.get(0);
            assertThat(either.operator()).isEqualTo(OR);
            assertThat(either.customParameters()).hasSize(0);
            var params = either.parameters();
            assertThat(params).hasSize(2);

            assertHasParamValue(params, isin, EQUALS, isinValue);
            assertHasParamValue(params, is_traded, EQUALS, stocksAreTraded);
        }

        @Test
        @DisplayName("removing unnecessary top-level `AND` predicate, " +
                "if there is just a single `OR` child predicate.")
        void removeUnnecessaryTopLevelAnd() {
            var query = queryManufacturer()
                    .either(r -> r.where(stock_count).is(42)
                                          .where(isin).is("some value"),
                            r -> r.where(stock_count).is(7)
                                  .where(isin).is("another value"))
                    .build();
            var topLevelOperator = query.subject()
                                        .predicate()
                                        .operator();
            assertThat(topLevelOperator).isEqualTo(OR);
        }

        @Test
        @DisplayName("sorted by the values of several columns")
        void withSorting() {
            var query = queryManufacturer()
                    .sortAscendingBy(when_founded)
                    .sortAscendingBy(isin)
                    .sortDescendingBy(is_traded)
                    .build();

            var sorting = query.sorting();
            assertThat(sorting).hasSize(3);
            assertThat(sorting.get(0)).isEqualTo(new SortBy<>(when_founded, ASC));
            assertThat(sorting.get(1)).isEqualTo(new SortBy<>(isin, ASC));
            assertThat(sorting.get(2)).isEqualTo(new SortBy<>(is_traded, DESC));
        }

        @Test
        @DisplayName("sorted by the values of several columns with the record limit")
        void withLimitAndSorting() {
            var tenRecords = 10;
            var query = queryManufacturer()
                    .sortDescendingBy(isin)
                    .sortAscendingBy(when_founded)
                    .limit(tenRecords)
                    .build();
            var sorting = query.sorting();
            assertThat(sorting.get(0)).isEqualTo(new SortBy<>(isin, DESC));
            assertThat(sorting.get(1)).isEqualTo(new SortBy<>(when_founded, ASC));
            assertThat(query.limit()).isEqualTo(tenRecords);
        }

        @Test
        @DisplayName("which return the same `Builder` instance if asked")
        void returnSameBuilder() {
            var builder = queryManufacturer()
                    .where(when_founded).isGreaterThan(THURSDAY)
                    .where(isin).is("JP 49869009911")
                    .sortAscendingBy(when_founded)
                    .limit(150);
            var query = builder.build();
            var actualBuilder = query.toBuilder();
            assertThat(actualBuilder).isSameInstanceAs(builder);
        }
    }

    @Nested
    @DisplayName("support filtering by given values")
    final class IsEqualTo {

        @Test
        @DisplayName("appending an `EQUALS` parameter via `isEqualTo`")
        void appendEqualsParameter() {
            var isinValue = "JP 3496600002";
            var parameters = queryManufacturer()
                    .where(isin).isEqualTo(isinValue)
                    .predicate()
                    .parameters();
            assertThat(parameters).hasSize(1);
            assertHasParamValue(parameters, isin, EQUALS, isinValue);
        }

        @Test
        @DisplayName("providing `is` alias for `isEqualTo`")
        void matchIs() {
            var isinValue = "JP 3496600002";
            var viaIs = queryManufacturer()
                    .where(isin).is(isinValue)
                    .predicate()
                    .parameters();
            var viaIsEqualTo = queryManufacturer()
                    .where(isin).isEqualTo(isinValue)
                    .predicate()
                    .parameters();
            assertThat(viaIsEqualTo).isEqualTo(viaIs);
        }
    }

    @Nested
    @DisplayName("prevent")
    final class Prevent {

        @Test
        @DisplayName("building queries with the record limit set without the sorting specified")
        void fromUsingLimitWithoutSorting() {
            assertThrows(IllegalStateException.class,
                         () -> queryManufacturer().limit(100)
                                                  .build());

        }
    }

    @Nested
    @DisplayName("return previously specified values")
    final class ReturnValues {

        @Test
        @DisplayName("of a single ID parameter")
        void ofId() {
            var value = manufacturerId();
            assertThat(queryManufacturer().id().is(value)
                                          .whichIds()
                                          .values()).containsExactly(value);
        }

        @Test
        @DisplayName("of several IDs")
        void ofSeveralIds() {
            var ids = generateIds(3);
            assertThat(queryManufacturer().id().in(ids)
                                          .whichIds()
                                          .values()).isEqualTo(ids);
        }

        @Test
        @DisplayName("of parameters")
        void ofParameterValues() {
            var isinValue = "JP 3496600002";
            var predicate = queryManufacturer()
                    .where(isin).is(isinValue)
                    .where(when_founded).isGreaterOrEqualTo(THURSDAY)
                    .predicate();
            assertThat(predicate.children()).isEmpty();
            assertThat(predicate.operator()).isEqualTo(AND);

            var parameters = predicate.parameters();
            assertHasParamValue(parameters, isin, EQUALS, isinValue);
            assertHasParamValue(parameters, when_founded, GREATER_OR_EQUALS, THURSDAY);
        }

        @Test
        @DisplayName("of a record limit")
        void ofLimit() {
            var limit = 55;
            assertThat(queryManufacturer().limit(limit)
                                          .whichLimit()).isEqualTo(limit);

        }

        @Test
        @DisplayName("of the sorting directives")
        void ofSorting() {
            assertThat(queryManufacturer().sortDescendingBy(isin)
                                          .sortAscendingBy(when_founded)
                                          .sorting())
                    .isEqualTo(ImmutableList.of(new SortBy<>(isin, DESC),
                                                new SortBy<>(when_founded, ASC))
                    );
        }
    }

    @Test
    @DisplayName("allow transforming the built `RecordQuery` instance" +
            " into an object of choice in the same call chain")
    void transform() {
        int predicateSize = queryManufacturer()
                .where(is_traded).is(false)
                .build((q) -> q.subject()
                               .predicate()
                               .parameters()
                               .size());
        assertThat(predicateSize).isEqualTo(1);
    }
}
