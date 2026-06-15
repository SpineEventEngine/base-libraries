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
