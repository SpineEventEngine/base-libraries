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

package io.spine.code.proto;

import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import com.google.protobuf.Descriptors.GenericDescriptor;

import java.util.Optional;

/**
 * A Protobuf option.
 *
 * @param <T>
 *         the type of the value held by this option
 * @param <K>
 *         the type of object that holds the option such as "field", "message", or "file"
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#custom_options">Protobuf
 *         Custom Options</a>
 */
@Immutable
public interface Option<@ImmutableTypeParameter T,
                        @ImmutableTypeParameter K extends GenericDescriptor> {

    /**
     * Obtains the value of this option for the specified object that holds it.
     *
     * @param object
     *         the option holder
     * @return value of this option
     */
    Optional<T> valueFrom(K object);

    /**
     * Checks if the option is declared on the given holder.
     *
     * @param object
     *         the option holder
     * @return {@code true} if the option is declared and is non-default, {@code false} otherwise
     */
    default boolean valuePresent(K object) {
        return valueFrom(object).isPresent();
    }
}
