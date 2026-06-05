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

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;

/**
 * A utility to work with Protobuf {@linkplain FieldDescriptorProto fields}.
 */
public final class FieldTypesProto {

    @SuppressWarnings("DuplicateStringLiteralInspection" /* The same string has different semantics. */)
    private static final String ENTRY_SUFFIX = "Entry";

    /** Prevents instantiation of this utility class. */
    private FieldTypesProto() {
    }

    /**
     * Checks the Protobuf field and determines it is a repeated field or not.
     *
     * <p>Although {@code map} fields technically count as {@code repeated}, this method will
     * return {@code false} for them.
     *
     * @param field
     *         the descriptor of the field to check
     * @return {@code true} if field is repeated, {@code false} otherwise
     */
    public static boolean isRepeated(FieldDescriptorProto field) {
        checkNotNull(field);
        var result = field.getLabel() == LABEL_REPEATED && !isMap(field);
        return result;
    }

    /**
     * Checks the Protobuf field and determines it is a map field or not.
     *
     * <p>If a field is a map, it is a repeated message with the specific type.
     *
     * @param field
     *         the descriptor of the field to check
     * @return {@code true} if field is map, {@code false} otherwise
     */
    public static boolean isMap(FieldDescriptorProto field) {
        checkNotNull(field);
        if (field.getLabel() != LABEL_REPEATED) {
            return false;
        }
        if (field.getType() != TYPE_MESSAGE) {
            return false;
        }
        var result = field.getTypeName()
                          .endsWith('.' + getEntryNameFor(field));
        return result;
    }

    /**
     * Constructs the entry name for the map field.
     *
     * <p>For example, a proto field with the name 'word_dictionary' has 'wordDictionary' JSON name.
     * Every map field has a corresponding entry type.
     * For 'word_dictionary' it would be 'WordDictionaryEntry'
     *
     * @param mapField
     *         the field to construct entry name
     * @return the name of the map field
     */
    public static String getEntryNameFor(FieldDescriptorProto mapField) {
        checkNotNull(mapField);
        var fieldName = FieldName.of(mapField);
        return fieldName.toCamelCase() + ENTRY_SUFFIX;
    }

    /**
     * Checks the Protobuf field and determines it is message type or not.
     *
     * @param fieldDescriptor
     *         the descriptor of the field to check
     * @return {@code true} if it is message, {@code false} otherwise
     */
    public static boolean isMessage(FieldDescriptorProto fieldDescriptor) {
        checkNotNull(fieldDescriptor);
        return fieldDescriptor.getType() == TYPE_MESSAGE;
    }
}
