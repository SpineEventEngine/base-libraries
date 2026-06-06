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

package io.spine.query;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.base.EntityState;

/**
 * An expression which sets the value to compare for the {@link EntityColumn} in scope of
 * a {@link EntityQueryBuilder} when building an {@link EntityQuery}.
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
     *         the column which actual value to use later in querying
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
