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

import com.google.protobuf.Descriptors.FieldDescriptor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

/**
 * A utility to work with Protobuf {@linkplain FieldDescriptor fields}.
 */
public final class FieldTypes {

    /**
     * The field of the {@code map} message type that represents the {@code map} key.
     */
    private static final String MAP_ENTRY_KEY = "key";

    /**
     * The field of the {@code map} message type that represents the {@code map} value.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") // Duplication with unrelated modules.
    private static final String MAP_ENTRY_VALUE = "value";

    /** Prevents instantiation of this utility class. */
    private FieldTypes() {
    }

    /**
     * Checks if the given field is of {@code message} type.
     *
     * @param field
     *         the descriptor of the field to check
     * @return {@code true} if the field is of {@code message} type, {@code false} otherwise
     */
    public static boolean isMessage(FieldDescriptor field) {
        checkNotNull(field);
        var isMessage = field.getType() == MESSAGE;
        return isMessage;
    }

    /**
     * Checks if the given field is a {@code repeated} proto field.
     *
     * <p>Although {@code map} fields technically count as {@code repeated}, this method will
     * return {@code false} for them.
     *
     * @param field
     *         the descriptor of the field to check
     * @return {@code true} if the field is a {@code repeated} proto field, {@code false} otherwise
     */
    public static boolean isRepeated(FieldDescriptor field) {
        checkNotNull(field);
        var proto = field.toProto();
        return FieldDescriptorProtos.isRepeated(proto);
    }

    /**
     * Checks if the given field is a {@code map} proto field.
     *
     * @param field
     *         the descriptor of the field to check
     * @return {@code true} if the field is a {@code map} proto field and {@code false} otherwise
     */
    public static boolean isMap(FieldDescriptor field) {
        checkNotNull(field);
        var proto = field.toProto();
        return FieldDescriptorProtos.isMap(proto);
    }

    /**
     * Obtains the key descriptor for the {@code map} field.
     *
     * <p>The descriptor type is {@link FieldDescriptor} because the map key technically is the
     * field of the {@code ...Entry} {@code message} type.
     *
     * @param field
     *         the {@code map} field for which to obtain key descriptor
     * @return the key descriptor for the specified {@code map} field
     * @throws IllegalStateException
     *         if the specified field is not a {@code map} proto field
     */
    public static FieldDescriptor keyDescriptor(FieldDescriptor field) {
        checkArgument(isMap(field),
                      "Trying to get key descriptor for the non-map field %s.",
                      field.getName());
        var descriptor = field.getMessageType()
                              .findFieldByName(MAP_ENTRY_KEY);
        return descriptor;
    }

    /**
     * Obtains the value descriptor for the {@code map} field.
     *
     * <p>The descriptor type is {@link FieldDescriptor} because the map value technically is the
     * field of the {@code ...Entry} {@code message} type.
     *
     * @param field
     *         the {@code map} field for which to obtain value descriptor
     * @return the value descriptor for the specified {@code map} field
     * @throws IllegalStateException
     *         if the specified field is not a {@code map} proto field
     */
    public static FieldDescriptor valueDescriptor(FieldDescriptor field) {
        checkArgument(isMap(field),
                      "Trying to get value descriptor for the non-map field %s.",
                      field.getName());
        var descriptor = field.getMessageType()
                              .findFieldByName(MAP_ENTRY_VALUE);
        return descriptor;
    }
}
