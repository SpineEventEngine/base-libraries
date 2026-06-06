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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count
import io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.when_founded
import java.util.function.UnaryOperator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Columns` should")
internal class ColumnsSpec {

    private val columns: Columns<Manufacturer> = Columns.of(is_traded, isin, stock_count)

    @Nested inner class
    `delegate read operations to the underlying list` {

        @Test
        fun size() {
            columns.size shouldBe 3
            columns.isEmpty() shouldBe false
        }

        @Test
        fun `element access`() {
            columns[0] shouldBe is_traded
            columns.indexOf(isin) shouldBe 1
            columns.lastIndexOf(stock_count) shouldBe 2
            columns.contains(is_traded) shouldBe true
            columns.contains(when_founded) shouldBe false
            columns.containsAll(listOf(is_traded, isin)) shouldBe true
        }

        @Test
        fun `sub-list and iterators`() {
            columns.subList(0, 2) shouldBe listOf(is_traded, isin)
            columns.iterator().hasNext() shouldBe true
            columns.listIterator().hasNext() shouldBe true
            columns.listIterator(1).hasNext() shouldBe true
            columns.spliterator().estimateSize() shouldBe 3L
        }

        @Test
        fun `stream operations`() {
            columns.stream().count() shouldBe 3L
            columns.parallelStream().count() shouldBe 3L
            var counted = 0
            // Pass an explicit `Consumer` to invoke the overridden Java `forEach`,
            // rather than Kotlin's `Iterable.forEach` extension.
            columns.forEach(java.util.function.Consumer { counted++ })
            counted shouldBe 3
        }

        @Test
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        fun `array conversion`() {
            // Routes through the overridden `toArray(T[])`.
            columns.toTypedArray().size shouldBe 3
            // Routes through the no-argument `toArray()`.
            (columns as java.util.List<*>).toArray().size shouldBe 3
        }

        @Test
        fun `string representation`() {
            columns.toString() shouldBe listOf(is_traded, isin, stock_count).toString()
        }
    }

    @Nested inner class
    `reject mutation, being immutable` {

        @Test
        fun `'add' by element`() {
            shouldThrow<UnsupportedOperationException> { columns.add(when_founded) }
        }

        @Test
        fun `'add' by index`() {
            shouldThrow<UnsupportedOperationException> { columns.add(0, when_founded) }
        }

        @Test
        fun addAll() {
            shouldThrow<UnsupportedOperationException> { columns.addAll(listOf(when_founded)) }
        }

        @Test
        fun `'addAll' by index`() {
            shouldThrow<UnsupportedOperationException> {
                columns.addAll(0, listOf(when_founded))
            }
        }

        @Test
        fun set() {
            shouldThrow<UnsupportedOperationException> { columns.set(0, when_founded) }
        }

        @Test
        fun `'remove' by element`() {
            shouldThrow<UnsupportedOperationException> { columns.remove(is_traded) }
        }

        @Test
        fun `'remove' by index`() {
            shouldThrow<UnsupportedOperationException> { columns.removeAt(0) }
        }

        @Test
        fun removeAll() {
            shouldThrow<UnsupportedOperationException> { columns.removeAll(listOf(is_traded)) }
        }

        @Test
        fun retainAll() {
            shouldThrow<UnsupportedOperationException> { columns.retainAll(listOf(is_traded)) }
        }

        @Test
        fun removeIf() {
            shouldThrow<UnsupportedOperationException> { columns.removeIf { true } }
        }

        @Test
        fun replaceAll() {
            shouldThrow<UnsupportedOperationException> {
                columns.replaceAll(UnaryOperator.identity())
            }
        }

        @Test
        fun sort() {
            shouldThrow<UnsupportedOperationException> {
                columns.sortWith(Comparator { _, _ -> 0 })
            }
        }

        @Test
        fun clear() {
            shouldThrow<UnsupportedOperationException> { columns.clear() }
        }
    }
}
