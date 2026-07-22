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
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
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
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
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
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
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
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
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
