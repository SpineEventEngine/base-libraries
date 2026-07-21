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
