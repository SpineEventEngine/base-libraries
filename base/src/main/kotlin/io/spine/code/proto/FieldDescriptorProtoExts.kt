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

@file:JvmName("FieldDescriptorProtos")

package io.spine.code.proto

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE

/**
 * Checks the Protobuf field and determines it is a repeated field or not.
 *
 * Although `map` fields technically count as `repeated`, this method will
 * return `false` for them.
 *
 * @return `true` if field is repeated, `false` otherwise.
 */
public fun FieldDescriptorProto.isRepeated(): Boolean =
    label == LABEL_REPEATED && !isMap()

/**
 * Checks the Protobuf field and determines it is a map field or not.
 *
 * If a field is a map, it is a repeated message with the specific type.
 *
 * @return `true` if field is map, `false` otherwise.
 */
public fun FieldDescriptorProto.isMap(): Boolean =
    label == LABEL_REPEATED && isMessage() && typeName.endsWith(".${entryName()}")

/**
 * Constructs the entry name for the map field.
 *
 * For example, a proto field with the name 'word_dictionary' has 'wordDictionary' JSON name.
 * Every map field has a corresponding entry type.
 * For 'word_dictionary' it would be 'WordDictionaryEntry'.
 *
 * @return the name of the map field.
 */
public fun FieldDescriptorProto.entryName(): String =
    FieldName.of(this).toCamelCase() + ENTRY_SUFFIX

/**
 * Checks the Protobuf field and determines it is a message type or not.
 *
 * @return `true` if it is a message, `false` otherwise.
 */
public fun FieldDescriptorProto.isMessage(): Boolean =
    type == TYPE_MESSAGE

/**
 * The suffix appended to the [camel-cased][FieldName.toCamelCase] field name to form
 * the name of the entry type generated for a `map` field.
 */
private const val ENTRY_SUFFIX: String = "Entry"
