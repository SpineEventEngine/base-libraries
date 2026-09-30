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

import com.google.protobuf.Message;

import java.io.Serial;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.type.PubPreconditions.requireInternal;
import static java.lang.String.format;

/**
 * Thrown when a Protobuf type having the {@code internal_type} option is sent
 * to a bounded context that does not declare it. Or, when the declaring bounded context attempts
 * to send this type outside.
 *
 * <p>A Java type corresponding to a Protobuf type with the {@code internal_type} option
 * is expected to be annotated as {@link io.spine.annotation.Internal Internal}.
 *
 * @see io.spine.annotation.Internal
 */
public class UnpublishedLanguageException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Creates an exception referencing the given type name.
     *
     * @param type
     *         the name of the type to be used in the message of the exception
     */
    public UnpublishedLanguageException(TypeName type) {
        super(formatMsg(type));
    }

    /**
     * Creates an exception with the name of the type of the given message.
     *
     * @param msg
     *         the message to report
     * @throws IllegalArgumentException
     *         if the message is not annotated as internal
     */
    public UnpublishedLanguageException(Message msg) {
        this(TypeName.of(requireInternal(msg)));
    }

    private static String formatMsg(TypeName type) {
        checkNotNull(type);
        return format(
                "The type `%s` is not a part of the published language" +
                        " of its bounded context." +
                        " As such it cannot be sent to/from outside this bounded context.", type
        );
    }
}
