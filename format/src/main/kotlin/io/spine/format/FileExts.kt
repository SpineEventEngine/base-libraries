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

@file:JvmName("Files")

package io.spine.format

import io.spine.format.Format.ProtoJson
import io.spine.io.replaceExtension
import java.io.File

/**
 * Tells if this file is of one of the supported [formats][Format].
 */
public fun File.hasSupportedFormat(): Boolean =
    Format.entries.any { it.matches(this) }

/**
 * Ensures that the file has the [primary extension][Format.extensions] of the given [format].
 *
 * @return `this` instance if the extension matches,
 *   a new instance with the required extension otherwise.
 */
@Suppress("ReturnCount") // Prefer earlier exits for better readability.
public fun File.ensureFormatExtension(format: Format<*>): File {
    if (format.matches(this)) {
        return this
    }
    val primary = format.extension
    // Handle the special case of `.pb.json`.
    val pbJson = ProtoJson.extension
    if (path.endsWith(pbJson)) {
        val newPath = path.replace(pbJson, primary)
        return File(newPath)
    }
    return replaceExtension(primary)
}
