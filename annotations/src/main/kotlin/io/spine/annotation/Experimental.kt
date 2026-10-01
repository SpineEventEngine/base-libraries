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

import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Indicates a public API that can change at any time and has no guarantee of API stability and
 * backward-compatibility.
 *
 * Here are the usage guidelines for this annotation:
 * 1. use only on public API. Internal interfaces should not use it.
 * 2. should be added only to new APIs. Adding it to an existing API is considered API-breaking.
 * 3. removing this annotation from an API gives it a stable status.
 *
 * @property value Context information such as links to discussion thread, tracking issue, etc.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    ANNOTATION_CLASS,
    CLASS,
    CONSTRUCTOR,
    FIELD,
    FILE,
    FUNCTION,
    PROPERTY
)
@MustBeDocumented
public annotation class Experimental(val value: String = "")
