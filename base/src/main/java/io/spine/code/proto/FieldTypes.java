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
