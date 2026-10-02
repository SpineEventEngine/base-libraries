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

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.query.ComparisonOperator
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`CartesianProducts` should")
internal class CartesianProductsSpec {

    @Nested internal inner class
    `multiply simple parameters over an 'OrExpression'` {

        @Test
        fun `producing a union of conjunctions`() {
            val param = RecordSubjectParameter(is_traded, ComparisonOperator.EQUALS, true)
            val params = listOf(param)
            
            val customColumn = object : CustomColumn<Manufacturer, String>() {
                override fun name(): ColumnName = ColumnName.of("custom")
                override fun type(): Class<String> = String::class.java
                override fun valueIn(source: Manufacturer): String = "value"
            }
            val customParam =
                CustomSubjectParameter(customColumn, "value", ComparisonOperator.EQUALS)

            val orExpression = OrExpression.newBuilder<Manufacturer>()
                .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 10))
                .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 20))
                .addCustomParam(customParam)
                .addExpression(AndExpression.newBuilder<Manufacturer>()
                    .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 30))
                    .build())
                .build()
            
            val result = OrExpression.newBuilder<Manufacturer>()
            CartesianProducts.cartesianSimpleParams(params, orExpression, result)
            
            val union = result.build()
            union.children() shouldHaveSize 4
        }
    }

    @Nested internal inner class
    `multiply child expressions over an 'OrExpression'` {

        @Test
        fun `producing a union of conjunctions`() {
            val child = AndExpression.newBuilder<Manufacturer>()
                .addParam(RecordSubjectParameter(is_traded, ComparisonOperator.EQUALS, true))
                .build()
            val children = listOf(child)
            
            val customColumn = object : CustomColumn<Manufacturer, String>() {
                override fun name(): ColumnName = ColumnName.of("custom")
                override fun type(): Class<String> = String::class.java
                override fun valueIn(source: Manufacturer): String = "value"
            }
            val customParam =
                CustomSubjectParameter(customColumn, "value", ComparisonOperator.EQUALS)

            val orExpression = OrExpression.newBuilder<Manufacturer>()
                .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 10))
                .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 20))
                .addCustomParam(customParam)
                .build()
            
            val result = OrExpression.newBuilder<Manufacturer>()
            CartesianProducts.cartesianChildren(children, orExpression, result)
            
            val union = result.build()
            union.children() shouldHaveSize 3
            union.children().forEach { 
                it.operator() shouldBe LogicalOperator.AND
            }
        }

        @Test
        fun `producing a union of conjunctions when child is an 'OrExpression'`() {
            val innerOr = OrExpression.newBuilder<Manufacturer>()
                .addParam(RecordSubjectParameter(is_traded, ComparisonOperator.EQUALS, true))
                .build()
            val children = listOf(innerOr)
            
            val orExpression = OrExpression.newBuilder<Manufacturer>()
                .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 10))
                .build()
            
            val result = OrExpression.newBuilder<Manufacturer>()
            CartesianProducts.cartesianChildren(children, orExpression, result)
            
            val union = result.build()
            union.children() shouldHaveSize 1
            val and = union.children()[0] as AndExpression
            and.children() shouldHaveSize 1
            and.children()[0] shouldBe innerOr
        }
    }

    @Nested internal inner class
    `multiply custom parameters over an 'OrExpression'` {

        @Test
        fun `producing a union of conjunctions`() {
            val column = object : CustomColumn<Manufacturer, String>() {
                override fun name(): ColumnName = ColumnName.of("custom")
                override fun type(): Class<String> = String::class.java
                override fun valueIn(source: Manufacturer): String = "value"
            }
            val param = CustomSubjectParameter(column, "value", ComparisonOperator.EQUALS)
            val params = listOf(param)
            
            val orExpression = OrExpression.newBuilder<Manufacturer>()
                .addParam(RecordSubjectParameter(stock_count, ComparisonOperator.EQUALS, 10))
                .addCustomParam(param)
                .addExpression(AndExpression.newBuilder<Manufacturer>()
                    .addParam(RecordSubjectParameter(is_traded, ComparisonOperator.EQUALS, true))
                    .build())
                .build()
            
            val result = OrExpression.newBuilder<Manufacturer>()
            CartesianProducts.cartesianCustomParams(params, orExpression, result)
            
            val union = result.build()
            union.children() shouldHaveSize 3
        }
    }

    @Test
    fun `have private constructor`() {
        val constructor = CartesianProducts::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        val instance = constructor.newInstance()
        instance shouldNotBe null
    }
}
