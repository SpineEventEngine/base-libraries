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

@file:JvmName("Parse")

package io.spine.format

import com.google.common.io.Files
import java.io.File

/**
 * Parses the given file loading the instance of the given class.
 *
 * The format of the file is determined by the extension of the file.
 *
 * @param T The type of instance stored in the file.
 * @param file The file to parse.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if reading the file fails,
 *  or if parsing of Protobuf-backed content fails.
 * @throws tools.jackson.core.JacksonException or its subclass, if parsing of
 *  Jackson-backed content ([Json][Format.Json] or [Yaml][Format.Yaml]) fails.
 * @throws ClassCastException if the stored value is not of the type [T].
 */
public inline fun <reified T : Any> parse(file: File): T =
    parse(file, T::class.java)

/**
 * Parses the given file loading the instance of the given class.
 *
 * This function provides the [format] parameter to cover the cases
 * of custom file extensions that are not assumed by
 * the supported [formats][Format.entries].
 *
 * @param T The type of instance stored in the file.
 * @param file The file to parse.
 * @param format The format of the file.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if reading the file fails,
 *  or if parsing of Protobuf-backed content fails.
 * @throws tools.jackson.core.JacksonException or its subclass, if parsing of
 *  Jackson-backed content ([Json][Format.Json] or [Yaml][Format.Yaml]) fails.
 * @throws ClassCastException if the stored value is not of the type [T].
 */
public inline fun <reified T : Any> parse(file: File, format: Format<in T>): T =
    parse(file, format, T::class.java)

/**
 * Parses the given file loading the instance of the given class.
 *
 * The format of the file is determined by the extension of the file.
 *
 * @param T The type of instance stored in the file.
 * @param file The file to parse.
 * @param cls The class of the instance stored in the file.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if reading the file fails,
 *  or if parsing of Protobuf-backed content fails.
 * @throws tools.jackson.core.JacksonException or its subclass, if parsing of
 *  Jackson-backed content ([Json][Format.Json] or [Yaml][Format.Yaml]) fails.
 * @throws ClassCastException if the file extension does not match the type
 *  of the [Format<T>][Format] specified by the [cls] parameter, or
 *  if the stored value is not of the type [T].
 */
public fun <T : Any> parse(file: File, cls: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    val format = Format.of(file) as Format<in T>
    return parse(file, format, cls)
}

/**
 * Parses the given file loading the instance of the given class.
 *
 * This function provides the [format] parameter to cover the cases
 * of custom file extensions that are not available from
 * the items of the [Format] enumeration.
 *
 * @param T The type of instance stored in the file.
 * @param file The file to parse.
 * @param format The format of the file.
 * @param cls The class of the instance stored in the file.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if reading the file fails,
 *  or if parsing of Protobuf-backed content fails.
 * @throws tools.jackson.core.JacksonException or its subclass, if parsing of
 *  Jackson-backed content ([Json][Format.Json] or [Yaml][Format.Yaml]) fails.
 * @throws ClassCastException if the stored value is not of the type [T].
 */
public fun <T : Any> parse(
    file: File,
    format: Format<in T>,
    cls: Class<T>
): T {
    val bytes = Files.asByteSource(file)
    val result = format.parser.parse(bytes, cls)
    return result
}
