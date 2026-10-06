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

import com.google.protobuf.Message
import com.google.protobuf.Any as AnyProto

/**
 * Unpacks this `Any` into the given message type.
 *
 * Prefer this extension function over the [com.google.protobuf.kotlin.unpack] extension,
 * which finds the default instance of the message reflectively each time it parses
 * the packed bytes. Both functions make this `Any` remember the unpacked message.
 * For more details, please see the documentation of the [AnyPacker] class.
 *
 * @param T The concrete type of the message stored in the `Any`.
 *   The type cannot be an interface or a non-final class.
 * @see unpackKnownType
 */
public inline fun <reified T : Message> AnyProto.unpack(): T {
    val cls = T::class
    require(cls.isFinal) {
        "Message type for the `unpack` call must be a concrete message, with a `final` class." +
                " `${cls.qualifiedName}` is not `final`." +
                " Please use `unpackKnownType()` if concrete message type is not available."
    }
    return AnyPacker.unpack(this, cls.java)
}

/**
 * Unpacks this `Any`.
 *
 * The concrete type of the message is looked up among
 * the [known types][io.spine.type.KnownTypes] by
 * the value of the `Any.type_url` field.
 *
 * @see AnyPacker.unpack
 */
public fun AnyProto.unpackKnownType(): Message =
    AnyPacker.unpack(this)

/**
 * Packs this message into an `Any`.
 */
public fun Message.pack(): AnyProto =
    AnyPacker.pack(this)
