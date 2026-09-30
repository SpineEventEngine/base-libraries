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

package io.spine.query.given;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import io.spine.query.ComparisonOperator;
import io.spine.query.Manufacturer;
import io.spine.query.ManufacturerId;
import io.spine.query.RecordColumn;
import io.spine.query.RecordColumns;
import io.spine.query.RecordQuery;
import io.spine.query.RecordQueryBuilder;
import io.spine.query.Subject;
import io.spine.query.SubjectParameter;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.RecordColumn.create;
import static io.spine.testing.TestValues.nullRef;
import static io.spine.testing.TestValues.randomString;

/**
 * Test environment data for {@link io.spine.query.RecordQueryBuilderTest RecordQueryBuilderTest}.
 */
public final class RecordQueryBuilderTestEnv {

    private RecordQueryBuilderTestEnv() {
    }

    /**
     * Creates a new instance of the query builder for the {@link Manufacturer} record message.
     */
    public static RecordQueryBuilder<ManufacturerId, Manufacturer> queryManufacturer() {
        return RecordQuery.newBuilder(ManufacturerId.class, Manufacturer.class);
    }

    /**
     * Generates a random {@link ManufacturerId}.
     */
    public static ManufacturerId manufacturerId() {
        return ManufacturerId.newBuilder()
                .setUuid(randomString())
                .build();
    }

    /**
     * Generates the given number of {@link ManufacturerId} instances.
     */
    public static ImmutableSet<ManufacturerId> generateIds(int howMany) {
        return IntStream.range(0, howMany)
                        .mapToObj((i) -> manufacturerId())
                        .collect(toImmutableSet());
    }

    /**
     * Asserts that the given query has no sorting, field mask and limit parameters set.
     */
    public static void assertNoSortingMaskLimit(RecordQuery<ManufacturerId, Manufacturer> query) {
        assertThat(query.sorting()).isEmpty();
        assertThat(query.mask()).isEqualTo(FieldMask.getDefaultInstance());
        assertThat(query.limit()).isEqualTo(nullRef());
    }

    /**
     * Checks that the query is not {@code null} as well as has no predicates and returns it.
     */
    public static Subject<ManufacturerId, Manufacturer>
    subjectWithNoParameters(RecordQuery<ManufacturerId, Manufacturer> query) {
        assertThat(query).isNotNull();
        var subject = query.subject();
        assertThat(subject.predicate().parameters()).isEmpty();
        return subject;
    }

    /**
     * Asserts the given list of the subject parameters has the parameter with the given properties.
     *
     * <p>In case there are several parameters for the same column, this method checks them all.
     *
     * @param list
     *         the list of all parameters
     * @param column
     *         the column for which the parameter value is asserted
     * @param operator
     *         the operator of the asserted parameter
     * @param value
     *         value of the parameter
     */
    public static void assertHasParamValue(List<SubjectParameter<Manufacturer, ?, ?>> list,
                                           RecordColumn<Manufacturer, ?> column,
                                           ComparisonOperator operator,
                                           Object value) {
        var parameterFound = false;
        for (var parameter : list) {
            if (parameter.column()
                         .equals(column)) {
                var actualOperator = parameter.operator();
                var actualValue = parameter.value();
                if (actualOperator == operator && value.equals(actualValue)) {
                    parameterFound = true;
                }
            }
        }
        assertThat(parameterFound).isTrue();
    }

    /**
     * Creates a new {@code FieldMask} with the name of the given column as a path.
     */
    public static FieldMask fieldMaskWith(RecordColumn<Manufacturer, ?> column) {
        return FieldMask.newBuilder()
                        .addPaths(column.name()
                                        .value())
                        .build();
    }

    /**
     * Defines the columns for {@link Manufacturer} message record.
     */
    @RecordColumns(ofType = Manufacturer.class)
    @SuppressWarnings("BadImport")  // for brevity.
    public static final class ManufacturerColumns {

        public static final RecordColumn<Manufacturer, String> isin =
                create("isin", String.class, (r) -> r.getIsin()
                                                     .getValue());

        public static final RecordColumn<Manufacturer, Timestamp> when_founded =
                create("when_founded", Timestamp.class, Manufacturer::getWhenFounded);

        public static final RecordColumn<Manufacturer, Boolean> is_traded =
                create("is_traded", Boolean.class, (r) -> !r.getStockSymbolList()
                                                            .isEmpty());

        public static final RecordColumn<Manufacturer, Integer> stock_count =
                create("stock_count", Integer.class, (r) -> r.getStockSymbolList()
                                                             .size());

        private ManufacturerColumns() {
        }
    }
}
