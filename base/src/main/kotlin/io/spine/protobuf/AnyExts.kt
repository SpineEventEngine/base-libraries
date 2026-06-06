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

import com.google.protobuf.Message
import com.google.protobuf.Any as AnyProto

/**
 * Unpacks this `Any` into the given message type.
 *
 * Prefer this extension function over the [com.google.protobuf.kotlin.unpack] extension
 * for the memory efficiency reasons. For more details on this recommendation, please see
 * the "Implementation Note" section of the [AnyPacker] class documentation.
 *
 * @param T the concrete type of the message stored in the `Any`. The type cannot
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
