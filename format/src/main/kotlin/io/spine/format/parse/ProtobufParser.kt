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

package io.spine.format.parse

import com.google.common.io.ByteSource
import com.google.protobuf.Message
import io.spine.type.fromJson
import io.spine.type.parse
import java.nio.charset.Charset.defaultCharset

/**
 * The abstract base for parsers of files containing Protobuf messages.
 */
internal sealed class ProtobufParser : Parser<Message> {

    override fun <M : Message> parse(source: ByteSource, cls: Class<out M>): M {
        val parsed = doParse(source, cls)
        @Suppress("UNCHECKED_CAST")
        return parsed as M
    }

    /**
     * Deserializes the given bytes into a message of the specified class.
     */
    abstract fun doParse(source: ByteSource, cls: Class<out Message>): Message
}

/**
 * The parser for Protobuf messages encoded in
 * the [binary Protobuf](https://protobuf.dev/programming-guides/encoding/) format.
 *
 * @see io.spine.format.write.ProtoBinaryWriter
 * @see io.spine.format.Format.ProtoBinary
 * @see ProtoJsonParser
 */
internal data object ProtoBinaryParser : ProtobufParser() {

    /**
     * Parses the [source] for obtaining the message of the specified class.
     */
    override fun doParse(source: ByteSource, cls: Class<out Message>): Message =
        source.openStream().use {
            return cls.parse(it)
        }
}

/**
 * The parser for Protobuf messages encoded in
 * the [ProtoJSON](https://protobuf.dev/programming-guides/json/) format.
 *
 * @see io.spine.format.write.ProtoJsonWriter
 * @see io.spine.format.Format.ProtoJson
 * @see ProtoBinaryParser
 */
internal data object ProtoJsonParser : ProtobufParser() {

    override fun doParse(source: ByteSource, cls: Class<out Message>): Message {
        val charSource = source.asCharSource(defaultCharset())
        val json = charSource.read()
        return cls.fromJson(json)
    }
}
