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

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Annotates a program element that exists, or is more widely visible than otherwise necessary,
 * only for use in test code.
 *
 * This annotation serves as a marker to indicate that the visibility of a class, method, field,
 * or other program element has been increased specifically to support testing.
 *
 * Apply this annotation to elements that would normally have more restricted visibility
 * (e.g., `private` or `internal`) but need to be more visible for testing purposes.
 *
 * This annotation does not actually restrict access to the annotated element.
 * It is purely informational and serves as documentation.
 *
 * Avoid using this annotation on `public` or `protected` API elements that are
 * part of your public contract. Instead, consider redesigning your API to avoid
 * exposing implementation details.
 *
 * @see TestOnly
 * @see Internal
 * @since 2.0.0
 */
@Retention(RUNTIME)
@Target(
    CLASS,
    CONSTRUCTOR,
    FIELD,
    FUNCTION,
    PROPERTY,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER
)
@MustBeDocumented
public annotation class VisibleForTesting
