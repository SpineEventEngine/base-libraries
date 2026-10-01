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
import io.spine.query.ComparisonOperator.EQUALS
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin
import io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`RecordCriterion` should")
internal class RecordCriterionSpec {

    @Test
    fun `add a parameter to its query builder when a value is set`() {
        val isinValue = "JP 3633400001"
        val builder: RecordQueryBuilder<ManufacturerId, Manufacturer> = queryManufacturer()

        val criterion: RecordCriterion<ManufacturerId, Manufacturer, String> = builder.where(isin)
        val query = criterion.`is`(isinValue).build()

        val params = query.subject().predicate().parameters()
        params shouldHaveSize 1

        val param = params.first()
        param.column() shouldBe isin
        param.operator() shouldBe EQUALS
        param.value() shouldBe isinValue
    }
}
