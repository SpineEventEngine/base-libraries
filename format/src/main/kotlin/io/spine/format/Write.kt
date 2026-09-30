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

@file:JvmName("Write")

package io.spine.format

import java.io.File

/**
 * Writes the given [value] using the specified format.
 *
 * The extension of the file is not checked to match the conventions
 * of the [Format] enumeration.
 *
 * To match the convention, please use [io.spine.format.ensureFormatExtension].
 */
public fun <T : Any> write(file: File, format: Format<in T>, value: T) {
    val writer = format.writer
    writer.write(file, value)
}
