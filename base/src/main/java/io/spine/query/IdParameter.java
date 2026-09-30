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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Sets the identifiers of objects that a {@link Query} targets.
 *
 * @param <I>
 *         the type of the identifiers
 */
@Immutable(containerOf = "I")
public final class IdParameter<I> {

    private final ImmutableSet<I> values;

    private IdParameter(ImmutableSet<I> values) {
        this.values = values;
    }

    /**
     * Returns the values of identifiers in this parameter.
     */
    public ImmutableSet<I> values() {
        return values;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("values", values)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdParameter)) {
            return false;
        }
        var parameter = (IdParameter<?>) o;
        return Objects.equals(values, parameter.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    /**
     * Creates a new instance of this parameter without restricting it to any identifier values.
     *
     * @param <I>
     *         the type of the values, to satisfy the contract of a calling party
     * @return a new instance of this type
     */
    public static <I> IdParameter<I> empty() {
        return new IdParameter<>(ImmutableSet.of());
    }

    /**
     * Creates a new instance restricting the parameter to a single identifier value.
     *
     * @param value
     *         the identifier value to use
     * @param <I>
     *         the type of the identifier value
     * @return a new instance of this type
     */
    public static <I> IdParameter<I> is(I value) {
        checkNotNull(value);
        return new IdParameter<>(ImmutableSet.of(value));
    }

    /**
     * Creates a new instance with the identifier values restricted to the passed.
     *
     * @param values
     *         the identifier values to use; must not be empty
     * @param <I>
     *         the type of the identifier values
     * @return a new instance of this type
     */
    public static <I> IdParameter<I> in(ImmutableSet<I> values) {
        checkNotNull(values);
        checkArgument(!values.isEmpty(), "Identifier values must not be empty.");
        return new IdParameter<>(values);
    }
}
