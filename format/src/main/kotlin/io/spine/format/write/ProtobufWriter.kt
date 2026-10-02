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

package io.spine.format.write

import com.google.protobuf.Message
import io.spine.format.Format.ProtoJson
import io.spine.type.toJson
import java.io.File

/**
 * The interface common to writers of Protobuf messages.
 */
internal interface ProtobufWriter: Writer<Message>

/**
 * Writes a message using the
 * [Protobuf binary format](https://protobuf.dev/programming-guides/encoding/).
 *
 * @see io.spine.format.Format.ProtoBinary
 * @see io.spine.format.parse.ProtoBinaryParser
 * @see ProtoJsonWriter
 */
internal object ProtoBinaryWriter : ProtobufWriter {

    override fun write(file: File, value: Message) =
        file.writeBytes(value.toByteArray())
}

/**
 * Writes a message using [ProtoJson] format.
 *
 * @see io.spine.format.parse.ProtoJsonParser
 * @see ProtoBinaryWriter
 * @see JsonWriter
 */
internal object ProtoJsonWriter : ProtobufWriter {

    override fun write(file: File, value: Message) =
        file.writeText(value.toJson())
}
