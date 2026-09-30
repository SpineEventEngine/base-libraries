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
