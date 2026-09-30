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
     * @throws java.io.IOException or its subclass, if reading the file fails,
     *   or if parsing of Protobuf-backed content fails.
     * @throws tools.jackson.core.JacksonException or its subclass, if parsing
     *   of Jackson-backed content ([Json][io.spine.format.Format.Json] or
     *   [Yaml][io.spine.format.Format.Yaml]) fails.
     */
    fun <R : T> parse(source: ByteSource, cls: Class<out R>): R
}
