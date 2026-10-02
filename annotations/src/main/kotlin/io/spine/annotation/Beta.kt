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
 * Signifies that a public API is subject to incompatible changes, or even removal
 * in a future release.
 *
 * An API bearing this annotation is exempt from any compatibility guarantees made by its
 * containing library. Note that the presence of this annotation implies nothing about the
 * quality of the API in question, only the fact that it is not "API-frozen."
 * It is generally safe for applications to depend on beta APIs at the cost of some extra work
 * during upgrades.
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
public annotation class Beta(val value: String = "")
