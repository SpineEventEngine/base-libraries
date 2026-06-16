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

package io.spine.format.parse

import com.google.common.io.ByteSource

/**
 * A parser for files in one of the supported [formats][io.spine.format.Format].
 *
 * ### API Note
 *
 * This interface is used internally for parsing any types from [ByteSource].
 * It should not be confused with [com.google.protobuf.Parser] that is used
 * in the Protobuf generated code for parsing [com.google.protobuf.Message] types
 * from various binary format inputs.
 *
 * @param T The type of the upper bound served by the parser.
 *   For example, if a parser supports parsing Protobuf message types,
 *   the argument would be [com.google.protobuf.Message].
 */
internal sealed interface Parser<T : Any> {

    /**
     * Attempts to deserialize the given settings value into the given class.
     *
     * @param R The type of the parsed value, which is a subtype of
     *   the type [T] supported by this parser.
     * @throws java.io.IOException or its subclass, if the parsing of the file fails.
     */
    fun <R : T> parse(source: ByteSource, cls: Class<out R>): R
}
