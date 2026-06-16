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
     * Creates an new instance of this parameter without restricting it to any identifier values.
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
