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
import io.spine.base.EntityState;

/**
 * An expression that sets the value to compare for the {@link EntityColumn} in scope of
 * an {@link EntityQueryBuilder} when building an {@link EntityQuery}.
 *
 * @param <S>
 *         the type of entity state
 * @param <V>
 *         the type of the column values for this criterion
 * @param <B>
 *         the type of the builder in scope of which this criterion exists
 */
public final class EntityCriterion<S extends EntityState<?>,
                                   V,
                                   B extends EntityQueryBuilder<?, S, B, ?>>
        extends QueryCriterion<S, V, EntityColumn<S, V>, B> {

    /**
     * Creates a new instance.
     *
     * @param column
     *         the column whose actual value is used later in querying
     * @param builder
     *         the builder of an {@link EntityQuery} in scope of which the criterion is created
     */
    public EntityCriterion(EntityColumn<S, V> column, B builder) {
        super(column, builder);
    }

    @Override
    @CanIgnoreReturnValue
    protected B addParameter(B builder,
                             EntityColumn<S, V> col,
                             ComparisonOperator operator, V value) {
        var parameter = new EntitySubjectParameter<>(col, value, operator);
        return builder.addParameter(parameter);
    }
}
