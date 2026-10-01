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

@file:JvmName("Binary")

package io.spine.type

import com.google.protobuf.Message
import com.google.protobuf.Parser
import io.spine.protobuf.defaultInstance
import io.spine.type.ExtensionRegistryHolder.extensionRegistry
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Obtains a binary form [Parser] for this message class.
 */
@Suppress("UNCHECKED_CAST") // The generated code ensures the correct parser type.
public val <M : Message> Class<M>.parser: Parser<M>
    get() = defaultInstance.parserForType as Parser<M>

/**
 * Creates a new message instance by parsing it from the given input stream.
 *
 * This function uses [ExtensionRegistry][extensionRegistry] with all known
 * custom Protobuf options.
 *
 * @param M The type of the message.
 * @see io.spine.type.ExtensionRegistryHolder
 */
public fun <M : Message> Class<M>.parse(input: InputStream): M =
    parser.parseFrom(input, extensionRegistry)

/**
 * Creates a new message instance by parsing it from the given input stream.
 *
 * This function uses [ExtensionRegistry][extensionRegistry] with all known
 * custom Protobuf options.
 *
 * @param M The type of the message.
 * @see io.spine.type.ExtensionRegistryHolder
 */
public fun <M : Message> KClass<M>.parse(input: InputStream): M =
    java.parse(input)
