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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.Message;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A converter handling the primitive types transformations.
 *
 * <p>Since the Protobuf and Java primitives differ, there may be more than one
 * {@code WrappingConverter} for a Java primitive type. In this case, if the resulting Protobuf
 * value type is not specified explicitly, the closest type is selected as a target for
 * the conversion. The closeness of two types is determined by the lexicographic closeness.
 *
 * @param <M>
 *         the type of the Protobuf primitive wrapper
 * @param <T>
 *         the type of the Java primitive wrapper
 * @implSpec It's sufficient to override methods {@link #wrap(Object) wrap(T)} and
 *         {@link #unwrap(Message) unwrap(M)} when extending this class.
 */
abstract class WrappingConverter<M extends Message, T> extends Converter<M, T> {

    @Override
    protected final T doForward(M input) {
        checkNotNull(input);
        return unwrap(input);
    }

    @Override
    protected final M doBackward(T input) {
        checkNotNull(input);
        return wrap(input);
    }

    /**
     * Unwraps a primitive value of type {@code T} from the given wrapper value.
     *
     * @param message
     *         wrapped value
     * @return unwrapped value
     */
    protected abstract T unwrap(M message);

    /**
     * Wraps the given primitive value into a Protobuf wrapper of type {@code M}.
     *
     * @param value
     *         primitive value
     * @return wrapped value
     */
    protected abstract M wrap(T value);
}
