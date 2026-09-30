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

package io.spine.base;

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.type.KnownMessage;

import java.util.UUID;

import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A common interface for the {@code string}-based unique identifiers.
 *
 * <p>The messages of suitable format are spotted by the Spine Model Compiler and marked with this
 * interface automatically.
 *
 * <p>By convention, a {@code string}-based identifier should have exactly one {@code string} field
 * named 'uuid':
 * <pre>
 *     {@code
 *         message ProjectId {
 *             // UUID-based generated value.
 *             string uuid = 1;
 *         }
 *     }
 * </pre>
 */
@Immutable
@GeneratedMixin
public interface UuidValue extends KnownMessage {

    /**
     * Verifies if the given UUID value is valid.
     *
     * @param uuid
     *         the value to check
     * @throws IllegalArgumentException
     *          if the given string is not a valid representation of UUID
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // We use `UUID.fromString()` only for checking.
    static void checkValid(String uuid) {
        checkNotEmptyOrBlank(uuid);
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw newIllegalArgumentException(e, "Invalid UUID string: `%s`.", uuid);
        }
    }
}
