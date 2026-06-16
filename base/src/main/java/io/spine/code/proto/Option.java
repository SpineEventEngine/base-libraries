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
