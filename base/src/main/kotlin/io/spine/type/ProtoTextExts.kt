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
