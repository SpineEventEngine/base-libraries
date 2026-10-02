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

@file:JvmName("FieldMasks")

package io.spine.protobuf

import com.google.protobuf.FieldMask
import com.google.protobuf.Message
import com.google.protobuf.util.FieldMaskUtil.fromFieldNumbers
import com.google.protobuf.util.FieldMaskUtil.isValid

/**
 * Constructs a [FieldMask] from the passed field numbers.
 *
 * @throws IllegalArgumentException
 *          if any of the fields are invalid for the message.
 */
public inline fun <reified T : Message> fromFieldNumbers(vararg fieldNumbers: Int): FieldMask =
    fromFieldNumbers<T>(fieldNumbers.toList())

/**
 * Constructs a [FieldMask] from the passed field numbers.
 *
 * @throws IllegalArgumentException
 *          if any of the fields are invalid for the message.
 */
public inline fun <reified T : Message> fromFieldNumbers(fieldNumbers: Iterable<Int>): FieldMask =
    fromFieldNumbers(T::class.java, fieldNumbers)

/**
 * Checks whether paths in a given fields mask are valid.
 */
public inline fun <reified T : Message> isValid(fieldMask: FieldMask): Boolean =
    isValid(T::class.java, fieldMask)

/**
 * Checks whether a given field path is valid.
 */
public inline fun <reified T : Message> isValid(fieldPath: String): Boolean =
    isValid(T::class.java, fieldPath)
