/*
 * Copyright 2025, TeamDev. All rights reserved.
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

import com.google.common.base.Converter
import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.google.protobuf.ProtocolMessageEnum
import io.spine.annotation.Internal

/**
 * Performs conversion of a [Protobuf Message][Message] to its Java counterpart and back.
 *
 * The inheritors implement the actual conversion to a specific [Message] and [Any].
 *
 * @param M The type of the message to convert.
 * @param T Target conversion type.
 */
@Internal
internal abstract class ProtoConverter<M : Message, T : Any> : Converter<M, T>() {

    override fun doForward(input: M): T = toObject(input)

    override fun doBackward(t: T): M = toMessage(t)

    /**
     * Converts supplied `input` message into a typed object.
     */
    protected abstract fun toObject(input: M): T

    /**
     * Converts supplied `input` object into a Protobuf message.
     */
    protected abstract fun toMessage(input: T): M

    companion object {

        /**
         * Returns a converter for the specified `type`.
         *
         * Protobuf [messages][Message] are returned [as is][AsIs].
         *
         * [ByteString] instances are [converted][BytesConverter] to
         * [BytesValue][com.google.protobuf.BytesValue].
         *
         * [Protobuf enums][ProtocolMessageEnum] are converted using a dedicated
         * [EnumConverter] which handles conversions by name or by number.
         *
         *  * [lists][List] are converted to [io.spine.base.ListOfAnys].
         *
         *  * [maps][Map] are converted to [io.spine.base.MapOfAnys].
         *
         * All other types are considered primitives and are
         * [handled][PrimitiveConverter] respectively.
         */
        @JvmStatic
        fun <M : Message, T : Any> forType(type: Class<T>): Converter<M, T> {
            val converter: ProtoConverter<*, *>
            if (Message::class.java.isAssignableFrom(type)) {
                converter = AsIs()
            } else if (ByteString::class.java.isAssignableFrom(type)) {
                converter = BytesConverter()
            } else if (isProtoEnum(type)) {
                @Suppress("UNCHECKED_CAST")
                val enumType = type as Class<out ProtocolMessageEnum>
                @Suppress("UNCHECKED_CAST")
                converter = EnumConverter(enumType as Class<Nothing>)
            } else if (List::class.java.isAssignableFrom(type)) {
                converter = ListConverter()
            } else if (Map::class.java.isAssignableFrom(type)) {
                converter = MapConverter()
            } else if (PrimitiveConverter.supportedPrimitives().contains(type)) {
                converter = PrimitiveConverter<Message, Any>()
            } else {
                throw UnsupportedOperationException(
                    "Cannot find a `ProtoConverter` for Protobuf type: `${type.canonicalName}`."
                )
            }
            @Suppress("UNCHECKED_CAST") // Logically checked.
            val result = converter as Converter<M, T>
            return result
        }

        private fun <T : Any> isProtoEnum(type: Class<T>): Boolean {
            return Enum::class.java.isAssignableFrom(type)
                    && ProtocolMessageEnum::class.java.isAssignableFrom(type)
        }
    }
}

/**
 * Returns the supplied `input` [Message] as is.
 */
private class AsIs : ProtoConverter<Message, Message>() {

    override fun toObject(input: Message): Message = input

    override fun toMessage(input: Message): Message = input
}
