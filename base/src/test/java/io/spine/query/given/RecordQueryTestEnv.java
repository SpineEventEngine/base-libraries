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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.query.Either;
import io.spine.query.Manufacturer;
import io.spine.query.ManufacturerId;
import io.spine.query.RecordPredicates;
import io.spine.query.RecordQueryBuilder;

import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.when_founded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer;

/**
 * Test environment for {@link io.spine.query.RecordQueryTest}.
 */
public final class RecordQueryTestEnv {

    private static final Timestamp NOW = Time.currentTime();

    private RecordQueryTestEnv() {
    }

    public static RecordPredicates<ManufacturerId, Manufacturer> conjunctivePredicates() {
        return (builder) -> builder.where(is_traded).is(false)
                                   .where(stock_count).is(0);
    }

    public static RecordPredicates<ManufacturerId, Manufacturer> moreConjunctivePredicates() {
        return (builder) -> builder.where(stock_count).is(15)
                                   .where(isin).is("More JP12341500");
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either1() {
        return r -> r.where(stock_count)
                     .is(10);
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either2() {
        return r -> r.where(when_founded)
                     .isLessThan(NOW);
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either3() {
        return r -> r.where(stock_count)
                     .is(99);
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either4() {
        return r -> r.where(when_founded)
                     .isGreaterOrEqualTo(Timestamp.getDefaultInstance());
    }

    @SuppressWarnings("unchecked")
    public static RecordQueryBuilder<ManufacturerId, Manufacturer>
    disjunctiveBuilder(Either<RecordQueryBuilder<ManufacturerId, Manufacturer>>... items) {
        return queryManufacturer()
                .either(items)
                .sortAscendingBy(isin)
                .limit(42);
    }

    @SuppressWarnings("unchecked")
    public static RecordPredicates<ManufacturerId, Manufacturer>
    disjunctivePredicates(Either<RecordQueryBuilder<ManufacturerId, Manufacturer>>... items) {
        var parameters = ImmutableList.copyOf(items);
        return (builder) -> builder.either(parameters);
    }

    public static RecordQueryBuilder<ManufacturerId, Manufacturer> conjunctiveBuilder() {
        var withPredicates = conjunctivePredicates().apply(queryManufacturer());
        return withSortingAndLimit(withPredicates);

    }

    public static RecordQueryBuilder<ManufacturerId, Manufacturer>
    withSortingAndLimit(RecordQueryBuilder<ManufacturerId, Manufacturer> builder) {
        return builder.sortAscendingBy(when_founded)
                      .limit(18);
    }
}
