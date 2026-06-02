/*
 * Copyright 2025, TeamDev. All rights reserved.
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

import com.google.common.testing.EqualsTester
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Empty
import com.google.protobuf.FieldMask
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.base.EntityState
import io.spine.testing.StubMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`EntityQuery` should")
internal class EntityQuerySpec {

    private val secondsColumn = EntityColumn<StubState, Long>(
        "seconds", Long::class.java, { 42L }
    )

    @Test
    fun `be converted back to builder`() {
        val builder = TestEntityQueryBuilder()
        val query = builder.build()
        query.toBuilder() shouldBe builder
    }

    @Test
    fun `be converted to 'RecordQuery'`() {
        val builder = TestEntityQueryBuilder()
        val criterion = EntityCriterion(secondsColumn, builder)
        criterion.`is`(100L)
        builder.withMask("seconds")
        builder.sortAscendingBy(secondsColumn)
        builder.limit(5)

        val query = builder.build()
        val recordQuery = query.toRecordQuery()

        recordQuery shouldNotBe null
        recordQuery.limit() shouldBe 5
        recordQuery.mask() shouldBe FieldMask.newBuilder().addPaths("seconds").build()
    }

    @Test
    fun `copy its state to another builder`() {
        val builder = TestEntityQueryBuilder()
        builder.sortAscendingBy(secondsColumn)
        builder.limit(5)
        val query = builder.build()

        val anotherBuilder = TestEntityQueryBuilder()
        query.copyTo(anotherBuilder)

        val anotherQuery = anotherBuilder.build()
        anotherQuery.limit() shouldBe 5
    }

    @Test
    fun `support 'equals()' and 'hashCode()'`() {
        val builder1 = TestEntityQueryBuilder()
        val query1a = builder1.build()
        val query1b = builder1.build()
        
        val builder2 = TestEntityQueryBuilder()
        builder2.limit(10)
        builder2.sortAscendingBy(secondsColumn)
        val query2 = builder2.build()
        
        EqualsTester()
            .addEqualityGroup(query1a, query1b)
            .addEqualityGroup(query2)
            .testEquals()
    }
}

/**
 * A stub entity state for testing purposes.
 */
internal class StubState : StubMessage(), EntityState<String> {
    override fun getDescriptorForType(): Descriptor = com.google.protobuf.Timestamp.getDescriptor()
    override fun getDefaultInstanceForType(): StubState = INSTANCE
    companion object {
        private val INSTANCE = StubState()
        @JvmStatic
        fun getDefaultInstance(): StubState = INSTANCE
    }
}

/**
 * A concrete implementation of [EntityQuery] for testing purposes.
 */
private class TestEntityQuery(builder: TestEntityQueryBuilder) :
    EntityQuery<String, StubState, TestEntityQueryBuilder>(builder)

/**
 * A concrete implementation of [EntityQueryBuilder] for testing purposes.
 */
private class TestEntityQueryBuilder :
    EntityQueryBuilder<String, StubState, TestEntityQueryBuilder, TestEntityQuery>(
        String::class.java, StubState::class.java
    ) {
    override fun thisRef(): TestEntityQueryBuilder = this
    override fun build(): TestEntityQuery = TestEntityQuery(this)
}
