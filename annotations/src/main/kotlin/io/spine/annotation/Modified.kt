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

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * The `Modified` annotation is used to mark the source code that was
 * [created][Generated] by one code generator and then
 * modified by another code generator hereby referenced as "modifier".
 *
 * The [value] element must contain the name of the modifier.
 * The recommended convention is to use the fully qualified name of
 * the corresponding component, such as a class.
 * Alternatively, consider using Maven coordinates of the "modifier".
 *
 * The [timestamp], which is optional, is the time when the code was modified.
 * It should follow the ISO 8601 standard.
 *
 * The [comments] element is the place for comments authors of code modifiers
 * may want to leave about the modifications made to the code.
 *
 * If changes that a modifier applies to original source files are significant and
 * are likely to overwhelm the value of the [comments] element,
 * authors of modifiers may consider putting a URL to the documentation that outlines
 * the nature of changes applied by the modifier.
 *
 * @property value Provides the name and other optional references
 *           (e.g., the version) to the code generator.
 * @property timestamp Optional date and time when the code was modified.
 * @property comments Optional comments about the nature of the modification
 *           made to the original code.
 *
 * @see Generated
 * @since 2.0.0
 */
@Retention(SOURCE)
@Target(
    ANNOTATION_CLASS,
    CLASS,
    CONSTRUCTOR,
    FIELD,
    FILE,
    FUNCTION,
    LOCAL_VARIABLE,
    PROPERTY,
    VALUE_PARAMETER
)
@MustBeDocumented
public annotation class Modified(
    vararg val value: String,
    val timestamp: String = "",
    val comments: String = ""
)
