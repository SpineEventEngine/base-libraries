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

@file:JvmName("ProtoTexts")

package io.spine.type

import com.google.protobuf.Message
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.TextFormat
import com.google.protobuf.TextFormat.Printer
import com.google.protobuf.TypeRegistry
import io.spine.string.Indent
import io.spine.string.Separator
import io.spine.string.pi
import io.spine.type.TypeRegistryHolder.typeRegistry

/**
 * Utilities for working with proto text format of Protobuf [Message] types.
 *
 * @see <a href="https://protobuf.dev/reference/protobuf/textformat-spec/">Protobuf
 * Text Format Language Specification</a>
 */
@Suppress("unused")
private const val ABOUT = ""

private object TextOutput {
    val printer: Printer by lazy {
        TextFormat.printer()
            .escapingNonAscii(true)
            .usingTypeRegistry(typeRegistry)
    }
}

/**
 * Generates a human-readable form of this message, useful for debugging and
 * other purposes, with no newline characters.
 *
 * The output is produced using [TypeRegistry] populated with [KnownTypes].
 */
public fun MessageOrBuilder.shortDebugString(): String =
    TextOutput.printer
        .emittingSingleLine(true)
        .printToString(this)

/**
 * Prints a textual representation of the `MessageOrBuilder` to the returned string.
 *
 * The text output contains only information about the message fields divided
 * by line separators. The name of the message type is not printed.
 *
 * The output is produced using [TypeRegistry] populated with [KnownTypes].
 *
 * @see MessageOrBuilder.printToStringWithName
 */
public fun MessageOrBuilder.printToString(): String =
    TextOutput.printer.printToString(this)

/**
 * Prints a textual representation of the `MessageOrBuilder` to the returned string.
 *
 * The output starts with the fully qualified name of the message type, followed
 * by a curly brace. Then follows the [text about the fields][printToString] indented
 * with two spaces. The output closes by a curly brace on the new line.
 *
 * The output is produced using [TypeRegistry] populated with [KnownTypes].
 *
 * @see MessageOrBuilder.printToString
 */
public fun MessageOrBuilder.printToStringWithName(): String {
    val typeName = descriptorForType.fullName
    val indent = Indent.defaultProtoTextIndent.value
    val fieldsBlock = printToString().pi(indent)
    val nl = Separator.nl()
    return buildString {
        append("$typeName {$nl")
        append(fieldsBlock)
        append("$nl}$nl")
    }
}
