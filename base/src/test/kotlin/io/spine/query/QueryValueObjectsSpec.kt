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

package io.spine.query

import com.google.common.collect.ImmutableSet
import com.google.common.testing.EqualsTester
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count
import io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Query value objects should")
internal class QueryValueObjectsSpec {

    @Test
    fun `compare 'RecordColumn' instances`() {
        val anotherBoolean = RecordColumn<Manufacturer, Boolean>(
            "other_flag", Boolean::class.javaObjectType
        ) { false }
        EqualsTester()
            .addEqualityGroup(is_traded)
            .addEqualityGroup(isin)
            .addEqualityGroup(anotherBoolean)
            .addEqualityGroup("not a column")
            .testEquals()
    }

    @Test
    fun `compare 'RecordColumn' instances that share a name but differ in getter`() {
        // Same name (so the super-class comparison passes) and same value type,
        // but different getters, exercising the field-by-field comparison.
        val first = RecordColumn<Manufacturer, String>("dup", String::class.java) { "a" }
        val second = RecordColumn<Manufacturer, String>("dup", String::class.java) { "b" }

        (first == second) shouldBe false
    }

    @Test
    fun `expose and compare 'SortBy' instances`() {
        val ascending = SortBy(stock_count, Direction.ASC)
        val sameAscending = SortBy(stock_count, Direction.ASC)
        val descending = SortBy(stock_count, Direction.DESC)

        ascending.column() shouldBe stock_count
        ascending.direction() shouldBe Direction.ASC

        EqualsTester()
            .addEqualityGroup(ascending, sameAscending)
            .addEqualityGroup(descending)
            .addEqualityGroup("not a sort")
            .testEquals()
    }

    @Test
    fun `compare 'IdParameter' instances`() {
        val first = IdParameter.`in`(ImmutableSet.of("a", "b"))
        val same = IdParameter.`in`(ImmutableSet.of("a", "b"))
        val different = IdParameter.`in`(ImmutableSet.of("c"))

        first.toString() shouldContain "values"

        EqualsTester()
            .addEqualityGroup(first, same)
            .addEqualityGroup(different)
            .addEqualityGroup("not a parameter")
            .testEquals()
    }

    @Test
    fun `render and compare 'SubjectParameter' instances`() {
        val first = RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 10)
        val same = RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 10)
        val different = RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 20)

        first.toString() shouldContain "stock_count"

        EqualsTester()
            .addEqualityGroup(first, same)
            .addEqualityGroup(different)
            .testEquals()
    }

    @Test
    fun `set an id criterion from vararg values`() {
        val first = manufacturerId()
        val second = manufacturerId()

        val query = RecordQuery.newBuilder(ManufacturerId::class.java, Manufacturer::class.java)
            .id().`in`(first, second)
            .build()

        query.subject().id().values() shouldBe ImmutableSet.of(first, second)
    }

    @Test
    fun `tell if a 'QueryPredicate' is empty and compare predicates`() {
        val empty = newBuilder().build().subject().predicate()
        empty.isEmpty shouldBe true

        val first = newBuilder().where(is_traded).`is`(true).build().subject().predicate()
        val same = newBuilder().where(is_traded).`is`(true).build().subject().predicate()
        first.isEmpty shouldBe false

        EqualsTester()
            .addEqualityGroup(first, same)
            .addEqualityGroup(empty)
            .addEqualityGroup("not a predicate")
            .testEquals()
    }

    private fun newBuilder() =
        RecordQuery.newBuilder(ManufacturerId::class.java, Manufacturer::class.java)
}
