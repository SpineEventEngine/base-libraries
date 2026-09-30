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

/**
 * This package provides utilities specific to enable Spine working with code files in code
 * generation, project structure analysis, and other tasks that are not covered by well-known
 * file management libraries.
 *
 * <p>For more file-related utilities, please see:
 * <ul>
 *     <li>{@link java.nio.file.Files Files} from NIO
 *     <li>{@link java.nio.file.Paths Paths} from NIO2
 *     <li>{@link com.google.common.io.Files Files from Guava}
 *     <li>{@link kotlin.io.FilesKt Kotlin I/O extensions from Utils.kt}
 * </ul>
 */
@CheckReturnValue
@NullMarked
package io.spine.io;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NullMarked;
