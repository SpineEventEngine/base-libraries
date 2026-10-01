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

@file:JvmName("FieldPaths")

@file:Suppress("MatchingDeclarationName") /* We want to keep the `FieldPathConstants` object
  under this file with the extensions because the constants declared in the object are closely
  related to the extensions. Having it as a top-level object would hurt readability.
  We also want to keep the name of the file according to the convention we have
  for the extension files.
*/

package io.spine.base

import io.spine.base.FieldPathConstants.REGEX
import io.spine.base.FieldPathConstants.SEPARATOR

/**
 * Constants related to parsing string representations of [FieldPath].
 */
public object FieldPathConstants {

    /**
     * The separator of path elements in a string representation.
     */
    public const val SEPARATOR: String = "."

    /**
     * The regular expression for string representation of a field path.
     *
     * The expression allows fields starting from capital letters as well.
     * This is to allow users to use custom field conventions should they have such a need.
     */
    public val REGEX: Regex = Regex("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$")
}

/**
 * Parses the given value into [FieldPath].
 */
public fun FieldPath(value: String): FieldPath = fieldPath {
    require(value.isNotEmpty()) {
        "A field path cannot be empty."
    }
    require(value.matches(REGEX)) {
        "The value `$value` does not match the expected format (`$REGEX`)."
    }
    value.split(SEPARATOR).forEach { fieldName.add(it) }
}

/**
 * Tells if this [FieldPath] doesn't denote a nested field.
 */
public val FieldPath.isNotNested: Boolean
    get() = fieldNameList.size == 1

/**
 * Tells if this [FieldPath] represents a nested field.
 */
public val FieldPath.isNested: Boolean
    get() = !isNotNested

/**
 * Returns this [FieldPath] as a single [String], where the field names
 * are separated with a dot.
 *
 * For example, `citizen.passport.first_name`.
 */
public val FieldPath.joined: String
    get() = fieldNameList.joinToString(SEPARATOR)

/**
 * Returns the root field's name of this [FieldPath].
 *
 * @throws NoSuchElementException if the path is empty.
 */
public val FieldPath.root: String
    get() = fieldNameList.first()

/**
 * Obtains the path immediately nested into this one.
 *
 * @throws IllegalStateException if this path is not nested.
 */
public fun FieldPath.stepInto(): FieldPath = fieldPath {
    check(isNested) {
        "Unable to step into the non-nested field path `$root`."
    }
    fieldName.addAll(fieldNameList.drop(1))
}
