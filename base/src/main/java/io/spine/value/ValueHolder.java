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

package io.spine.value;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract base for objects that hold {@code Serializable} values.
 *
 * @param <T> a type of value enclosed in the holder
 */
public abstract class ValueHolder<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
    private final T value;

    protected ValueHolder(T value) {
        this.value = checkNotNull(value);
    }

    /**
     * Returns the stored value.
     *
     * <p>Overriding methods may perform additional type conversion, if needed.
     */
    public T value() {
        return getValue();
    }

    /**
     * Obtains the value passed to the holder during construction.
     */
    public final T getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValueHolder)) {
            return false;
        }
        var other = (ValueHolder<?>) obj;
        return Objects.equals(this.value, other.value);
    }
}
