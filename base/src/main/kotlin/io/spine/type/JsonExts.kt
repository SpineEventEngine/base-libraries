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

@file:JvmName("Json")

package io.spine.type

import com.google.common.base.Throwables
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.util.JsonFormat.Parser
import com.google.protobuf.util.JsonFormat.Printer
import io.spine.protobuf.builderFor
import io.spine.type.TypeRegistryHolder.typeRegistry
import io.spine.util.Exceptions.newIllegalStateException

/**
 * Utilities for working with JSON representation of Protobuf [Message] types.
 *
 * Both [parsing][Class.fromJson] and [printing][Message.toJson] functions assume
 * the presence of the custom Protobuf message types relying on [KnownTypes] for this.
 *
 * The parsing functionality follows the default Protobuf strategy for
 * [ignoring][Parser.ignoringUnknownFields] unknown fields when a JSON string is parsed.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#unknowns">
 *         Protobuf Unknown Fields</a>
 */
@Suppress("unused")
private const val ABOUT = ""

/**
 * Holds lazily evaluated properties related to generating and parsing JSON.
 */
private object JsonOutput {

    val printer: Printer by lazy {
        JsonFormat.printer()
            .usingTypeRegistry(typeRegistry)
    }

    val compactPrinter: Printer by lazy {
        printer.omittingInsignificantWhitespace()
    }

    val parser: Parser by lazy {
        JsonFormat.parser()
            .ignoringUnknownFields()
            .usingTypeRegistry(typeRegistry)
    }
}

/**
 * Converts this message or builder to JSON using the given [printer].
 *
 * The default instance of the [Printer] produces multi-line output.
 *
 * @see [toCompactJson]
 */
@JvmOverloads
public fun MessageOrBuilder.toJson(printer: Printer = JsonOutput.printer): String {
    val result: String?
    try {
        result = printer.print(this)
    } catch (e: InvalidProtocolBufferException) {
        val rootCause = Throwables.getRootCause(e)
        throw UnknownTypeException(rootCause)
    }
    check(result != null)
    return result
}

/**
 * Converts this message into a compact JSON representation.
 *
 * <p>The result JSON does not contain the line separators.
 *
 * @see [toJson]
 */
public fun MessageOrBuilder.toCompactJson(): String =
    toJson(JsonOutput.compactPrinter)

/**
 * Parses a message of the type [T] from the given [json] representation.
 *
 * @throws IllegalArgumentException if the message of this type cannot be parsed
 *  from the given string.
 */
public fun <T : Message> Class<T>.fromJson(json: String): T {
    try {
        val messageBuilder = builderFor(this)
        JsonOutput.parser.merge(json, messageBuilder)
        @Suppress("UNCHECKED_CAST") // The type is ensured by `builderFor()`.
        val result = messageBuilder.build() as T
        return result
    } catch (e: InvalidProtocolBufferException) {
        throw newIllegalStateException(
            e,
            "The JSON text (`$json`) cannot be parsed to an instance of the class `$name`."
        )
    }
}
