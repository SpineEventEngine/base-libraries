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

package io.spine.type;

import static java.lang.String.format;

/**
 * Thrown when the type packed into {@link com.google.protobuf.Any Any} does not
 * match one we expect when unpacking.
 *
 * <p>Typically this exception wraps
 * {@link com.google.protobuf.InvalidProtocolBufferException InvalidProtocolBufferException} thrown
 * in unsuccessful call of {@link com.google.protobuf.Any#unpack(Class) Any.unpack(Class)}.
 *
 * <p>Another usage scenario is a mismatch between
 * the {@linkplain TypeUrl}s of the instance wrapped by {@code Any} and the target message.
 */
public class UnexpectedTypeException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    /**
     * Creates an instance of {@code UnexpectedTypeException} by wrapping the root cause.
     */
    public UnexpectedTypeException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance of {@code UnexpectedTypeException} with the expected and actual type URLs.
     */
    public UnexpectedTypeException(TypeUrl expected, TypeUrl actual) {
        super(formatMsg(expected, actual));
    }

    private static String formatMsg(TypeUrl expected, TypeUrl actual) {
        return format("Cannot unpack `Any` instance. Expected type name is `%s`, actual is `%s`.",
                      expected, actual);
    }
}
