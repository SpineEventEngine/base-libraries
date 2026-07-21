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
