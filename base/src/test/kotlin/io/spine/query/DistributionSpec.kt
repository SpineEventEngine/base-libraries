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

import io.kotest.matchers.shouldBe
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Distribution` should apply the distributive law to")
internal class DistributionSpec {

    private val customColumn = object : CustomColumn<Manufacturer, String>() {
        override fun name(): ColumnName = ColumnName.of("custom")
        override fun type(): Class<String> = String::class.java
        override fun valueIn(source: Manufacturer): String = "value"
    }

    private fun simpleParam(value: Int) =
        RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, value)

    private fun tradedParam() =
        RecordSubjectParameter(is_traded, ComparisonOperator.EQUALS, true)

    private fun customParam() =
        CustomSubjectParameter(customColumn, "value", ComparisonOperator.EQUALS)

    private fun and(value: Int): AndExpression<Manufacturer> =
        AndExpression.newBuilder<Manufacturer>()
            .addParam(simpleParam(value))
            .build()

    /** An `OR` carrying simple params, custom params, an `AND` child and an `OR` child. */
    private fun richOr(): OrExpression<Manufacturer> =
        OrExpression.newBuilder<Manufacturer>()
            .addParam(simpleParam(1))
            .addCustomParam(customParam())
            .addExpression(and(2))
            .addExpression(
                OrExpression.newBuilder<Manufacturer>()
                    .addParam(tradedParam())
                    .build()
            )
            .build()

    @Test
    fun `combine two AND expressions by concatenation`() {
        val result = Distribution.conjunctive(and(1), and(2))
        result.operator() shouldBe LogicalOperator.AND
    }

    @Test
    fun `combine an AND with an OR expression into a disjunction`() {
        val result = Distribution.conjunctive(and(7), richOr())
        result.operator() shouldBe LogicalOperator.OR
    }

    @Test
    fun `combine an OR with an AND expression into a disjunction`() {
        val result = Distribution.conjunctive(richOr(), and(7))
        result.operator() shouldBe LogicalOperator.OR
    }

    @Test
    fun `combine two OR expressions by Cartesian product`() {
        val result = Distribution.conjunctive(richOr(), richOr())
        result.operator() shouldBe LogicalOperator.OR
    }
}
