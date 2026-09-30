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
