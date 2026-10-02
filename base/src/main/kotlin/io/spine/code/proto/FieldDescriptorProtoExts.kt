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
