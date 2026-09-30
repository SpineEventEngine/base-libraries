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
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.FieldMask
import com.google.protobuf.Timestamp
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.base.EntityState
import io.spine.base.Field
import io.spine.base.SubscribableField
import io.spine.testing.StubMessage
import java.io.Serial
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
    fun `apply a field mask defined by subscribable fields`() {
        val builder = TestEntityQueryBuilder()
        val field = Field.named("seconds")
        val subscribableField = object : SubscribableField(field) {}
        builder.withMask(subscribableField)

        val query = builder.build()

        query.mask() shouldBe FieldMask.newBuilder().addPaths(field.toString()).build()
    }

    @Test
    fun `expose the sorting direction of its columns`() {
        val ascending = TestEntityQueryBuilder()
            .apply { sortAscendingBy(secondsColumn) }
            .build()
        ascending.sorting().first().direction() shouldBe Direction.ASC

        val descending = TestEntityQueryBuilder()
            .apply { sortDescendingBy(secondsColumn) }
            .build()
        descending.sorting().first().direction() shouldBe Direction.DESC
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

    @Test
    fun `apply a custom column criterion`() {
        val customColumn = object : CustomColumn<StubState, Long>() {
            override fun name(): ColumnName = ColumnName.of("custom_seconds")
            override fun type(): Class<Long> = Long::class.java
            override fun valueIn(source: StubState): Long = 0L
        }
        val builder = TestEntityQueryBuilder()

        builder.where(customColumn, 100L)
        builder.withMask(Field.named("seconds"))

        val query = builder.build()
        query.subject().predicate().customParameters().shouldNotBeEmpty()
    }

    @Test
    fun `build a transformed query`() {
        val builder = TestEntityQueryBuilder()
        val transformed = builder.build { _ -> "transformed-result" }
        transformed shouldBe "transformed-result"
    }
}

/**
 * A stub entity state for testing purposes.
 */
internal class StubState : StubMessage(), EntityState<String> {
    override fun getDescriptorForType(): Descriptor = Timestamp.getDescriptor()
    override fun getDefaultInstanceForType(): StubState = INSTANCE
    companion object {
        @Serial
        private const val serialVersionUID: Long = 0L
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
