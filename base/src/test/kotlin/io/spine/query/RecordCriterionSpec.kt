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
