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

import kotlin.annotation.AnnotationRetention.BINARY
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
 * The `Generated` annotation is used to mark source code that has been generated.
 *
 * When applied to a type, it means the whole type was generated.
 * It can be also applied to an element of Java or Kotlin code to differentiate
 * generated code from handcrafted.
 *
 * The [value] element must contain the name of the generator.
 * The recommended convention is to use the fully qualified name of
 * the corresponding component, such as a class.
 * Alternatively, consider using Maven coordinates of the "generator".
 *
 * The [timestamp], which is optional, is the time when the code was generated.
 * It should follow the ISO 8601 standard.
 *
 * The [comments] element is the place for comments authors of code generators
 * may want to leave about the generated code.
 *
 * ### About the name
 *
 * The name of this annotation type must be `Generated` so that development tools
 * exclude the generated code from test coverage.
 *
 * [JaCoCo analyzes](https://github.com/jacoco/jacoco/issues/685#issuecomment-392020612)
 * Java classes for the presence of an annotation whose simple name is `Generated`.
 * It cannot be `GrpcGenerated` or `SpineGenerated`.
 * It could be any package, but the simple name must be `Generated`.
 * Such a class is automatically excluded from the report.
 *
 * @property value Provides the name and other optional references (e.g., the version)
 *           to the code generator.
 * @property timestamp Optional date and time when the code was generated.
 * @property comments Optional comments that the code generator may want to include.
 *
 * @see Modified
 * @since 2.0.0
 */
@Retention(BINARY)
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
public annotation class Generated(
    vararg val value: String,
    val timestamp: String = "",
    val comments: String = ""
)
