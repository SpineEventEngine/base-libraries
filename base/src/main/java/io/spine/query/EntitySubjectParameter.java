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

import io.spine.base.EntityState;

/**
 * A parameter defining how to query a record of an entity state by the value
 * of its {@linkplain EntityColumn column}.
 *
 * @param <S>
 *         the type of entity state
 * @param <V>
 *         the type of the entity column values
 */
final class EntitySubjectParameter<S extends EntityState<?>, V>
        extends SubjectParameter<S, EntityColumn<S, V>, V> {

    /**
     * Creates an instance of the parameter targeting entities whose column value is compared
     * to the one provided in a specified way.
     *
     * @param column
     *         the column to query
     * @param value
     *         the column value to use when querying
     * @param operator
     *         the operator to use when comparing the actual column value to the provided one
     */
    EntitySubjectParameter(EntityColumn<S, V> column, V value, ComparisonOperator operator) {
        super(column, operator, value);
    }
}
