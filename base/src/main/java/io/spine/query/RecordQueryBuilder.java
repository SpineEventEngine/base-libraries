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

package io.spine.query;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.annotation.SPI;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A builder for {@link RecordQuery}.
 *
 * @param <I>
 *         the type of identifiers of the queried records
 * @param <R>
 *         the type of the queried records
 */
@SPI
public class RecordQueryBuilder<I, R extends Message>
        extends AbstractQueryBuilder<I,
                                     R,
                                     RecordSubjectParameter<R, ?>,
                                     RecordQueryBuilder<I, R>,
                                     RecordQuery<I, R>> {

    protected RecordQueryBuilder(Class<I> idType, Class<R> recordType) {
        super(idType, recordType);
    }

    @Override
    protected RecordQueryBuilder<I, R> thisRef() {
        return this;
    }

    /**
     * Creates a new instance of a corresponding query using the data of this builder.
     *
     * <p>If the {@linkplain #limit(int) record limit} is set, checks that at least
     * one sorting directive is present. Otherwise throws an {@linkplain IllegalStateException}.
     */
    @Override
    public RecordQuery<I, R> build() {
        return new RecordQuery<>(this);
    }

    /**
     * Builds a query on top of this record query builder and transforms it according
     * to the logic of the passed transformer.
     *
     * <p>This method is a syntax sugar for a convenient method chaining for those who wish to use
     * the produced query in their own transformation flow.
     *
     * @param transformer
     *         function transforming the query
     * @param <T>
     *         the type of the resulting object
     * @return a transformed query instance
     */
    public <T> T build(Function<RecordQuery<?, ?>, T> transformer) {
        checkNotNull(transformer);
        var query = build();
        var result = transformer.apply(query);
        return result;
    }

    /**
     * Creates a criterion for a particular record column.
     *
     * @param column
     *         the record column that will be queried
     * @param <V>
     *         the type of the record column values
     * @return a new criterion for the given column
     */
    @CanIgnoreReturnValue
    public <V> RecordCriterion<I, R, V> where(RecordColumn<R, V> column) {
        return new RecordCriterion<>(column, this);
    }

    /**
     * Creates a criterion for the identifier values of the queried records.
     *
     * @return a new instance of a criterion
     */
    public IdCriterion<I, RecordQueryBuilder<I, R>> id() {
        return new IdCriterion<>(thisRef());
    }
}
