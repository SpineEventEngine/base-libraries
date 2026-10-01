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

package io.spine.format.write

import io.spine.format.Format
import java.io.File

/**
 * The interface common for classes that write data to a file.
 */
internal interface Writer<T : Any> {

    /**
     * Writes the [value] into the file.
     *
     * The extension of the file is not checked to match the conventions
     * of the [Format] to which this writer belongs.
     *
     * @see io.spine.format.ensureFormatExtension
     */
    fun write(file: File, value: T)
}
