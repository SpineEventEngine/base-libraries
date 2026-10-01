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

import com.google.errorprone.annotations.Immutable;

import java.io.Serial;

/**
 * Abstract base for classes holding a value of a {@link Class}.
 *
 * @param <T> the type of the class
 * @apiNote The name of this class has the 'Type' infix to prevent the clash with
 *          {@link java.lang.ClassValue ClassValue}.
 */
@Immutable
public abstract class ClassTypeValue<T> extends ValueHolder<Class<? extends T>> {

    /* NOTE: the class has the 'Type' infix in the name to prevent the name clash with
       java.lang.ClassValue. */

    @Serial
    private static final long serialVersionUID = 0L;

    protected ClassTypeValue(Class<? extends T> value) {
        super(value);
    }

    /**
     * Returns {@linkplain Class#getName() the name} of the enclosed class value.
     *
     * @return the value class name
     */
    @Override
    public String toString() {
        return value().getName();
    }
}
