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

import com.google.common.testing.EqualsTester
import com.google.protobuf.FieldMask
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.spine.base.Field
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin
import io.spine.query.given.RecordQueryBuilderTestEnv.fieldMaskWith
import io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Holds the Kotlin tests for [RecordQueryBuilder], complementing the Java
 * `RecordQueryBuilderTest`.
 *
 * New tests for [RecordQueryBuilder] should be added here as the codebase migrates
 * test code to Kotlin.
 *
 * @see io.spine.query.RecordQueryBuilderTest
 */
@DisplayName("`RecordQueryBuilder` should")
internal class RecordQueryBuilderSpec {

    @Suppress("DEPRECATION") // Tests the deprecated API.
    @Nested inner class
    `ignore field masks` {

        private val mask = fieldMaskWith(is_traded)
        private val paths = arrayOf("isin", "when_founded")
        private val field = Field.named("stock_count")

        @Test
        fun `returning the same builder`() {
            val builder = queryManufacturer()

            builder.withMask(mask) shouldBeSameInstanceAs builder
            builder.withMask(*paths) shouldBeSameInstanceAs builder
            builder.withMask(field) shouldBeSameInstanceAs builder
        }

        @Test
        fun `not passing them to the query`() {
            val builder = queryManufacturer()
                .withMask(mask)
                .withMask(*paths)
                .withMask(field)

            builder.whichMask().shouldBeEmpty()
            builder.build().mask() shouldBe FieldMask.getDefaultInstance()
        }

        @Test
        fun `keeping the query equal to the one without a mask`() {
            val isinValue = "JP 3633400001"
            val withoutMask = queryManufacturer()
                .where(isin).`is`(isinValue)
                .build()
            val withMask = queryManufacturer()
                .where(isin).`is`(isinValue)
                .withMask(mask)
                .build()

            EqualsTester()
                .addEqualityGroup(withoutMask, withMask)
                .testEquals()
        }

        @Test
        fun `with unknown paths`() {
            shouldNotThrowAny {
                queryManufacturer().withMask("no_such_field")
            }
        }

        @Test
        fun `set inside 'either()'`() {
            shouldNotThrowAny {
                queryManufacturer()
                    .either(Either { it.withMask(mask) })
                    .build()
            }
        }
    }
}
