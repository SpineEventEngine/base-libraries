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

import java.io.Serial;

import static java.lang.String.format;

/**
 * Exception that is thrown when an unsupported message is obtained
 * or in case when there is no class for the given Protobuf message.
 */
public class UnknownTypeException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Creates a new instance with the type name.
     *
     * @param typeName the unknown type
     */
    public UnknownTypeException(String typeName) {
        super(makeMsg(typeName));
    }

    private static String makeMsg(String typeName) {
        return format("No Java class found for the Protobuf message of type: `%s`.", typeName);
    }

    /**
     * Creates a new instance with the type name and the cause.
     *
     * @param typeName the unknown type
     * @param cause    the exception cause
     */
    public UnknownTypeException(String typeName, Throwable cause) {
        super(makeMsg(typeName), cause);
    }

    /**
     * Creates a new instance when only the cause is known.
     *
     * <p>Use this constructor when propagating
     * {@link com.google.protobuf.InvalidProtocolBufferException InvalidProtocolBufferException}
     * without knowing which type caused the exception
     * (e.g., when calling {@code JsonFormat.print()}).
     */
    public UnknownTypeException(Throwable cause) {
        super(cause);
    }
}
