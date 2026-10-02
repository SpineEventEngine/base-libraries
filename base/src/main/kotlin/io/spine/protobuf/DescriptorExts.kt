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

package io.spine.protobuf

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import io.spine.string.camelCase

/**
 * Obtains a descriptor of the field with the given [name] or `null` if there is no such field.
 */
public fun Descriptor.field(name: String): FieldDescriptor? = findFieldByName(name)

/**
 * Obtains a descriptor of the field with the given [number] or `null` if there is no such field.
 */
public fun Descriptor.field(number: Int): FieldDescriptor? = findFieldByNumber(number)

/**
 * Obtains only descriptors of message types declared under the message represented
 * by this descriptor.
 *
 * The method filters synthetic descriptors created for map fields.
 * A descriptor of a map field entry is named after the name of the field
 * with the `"Entry"` suffix.
 * We use this convention for filtering [Descriptor.nestedTypes] returned by the Protobuf API.
 *
 * @see <a href="https://protobuf.dev/programming-guides/proto3/#maps-features">
 *     Protobuf documentation</a>
 */
public fun Descriptor.realNestedTypes(): List<Descriptor> {
    val mapEntryTypes = fields.filter { it.isMapField }
        .map { it.name.camelCase() + "Entry" }.toList()
    return nestedTypes.filter { !mapEntryTypes.contains(it.name) }
}
