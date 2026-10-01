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

package io.spine.annotation

/**
 * This class shows using the [Modified] annotation with single line in Kotlin.
 */
@Suppress("EmptyClassBlock", "unused")
@Modified("Single line")
private class ModifiedWithSingleLine

/**
 * This class shows using the [Modified] annotation with
 * multi-line [Modified.value] argument in Kotlin.
 */
@Suppress("EmptyClassBlock", "unused")
@Modified("Line 1", "Line 2", "Line 3", timestamp = "2025-09-17", comments = "Some comments")
private class ModifiedMultipleLines

/**
 * This class shows using the [Modified] annotation with
 * multi-line [Modified.value] argument in Kotlin when
 * the [value][Modified.value] parameter is named.
 */
@Suppress("EmptyClassBlock", "unused")
@Modified(value = ["Line 1", "Line 2", "Line 3"], timestamp = "12:45", comments = "Some comments")
private class ModifiedMultipleLinesInArray
