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

package io.spine.string;

import com.google.common.base.Converter;

/**
 * Serves as a converter from {@code I} to {@code String} with an associated
 * reverse function from {@code String} to {@code I}.
 *
 * <p>It is used for converting back and forth between the different
 * representations of the same information.
 *
 * @param <T> the type of converted objects
 * @see #convert(Object)
 * @see #reverse()
 */
public abstract class Stringifier<T> extends Converter<T, String> {

    /**
     * Converts the thing to a string.
     */
    protected abstract String toString(T obj);

    /**
     * Converts the string back to a thing.
     */
    protected abstract T fromString(String s);

    /**
     * Invokes {@link #toString(Object)}.
     */
    @Override
    protected final String doForward(T obj) {
        return toString(obj);
    }

    /**
     * Invokes {@link #fromString(String)}.
     */
    @Override
    protected final T doBackward(String str) {
        return fromString(str);
    }
}
