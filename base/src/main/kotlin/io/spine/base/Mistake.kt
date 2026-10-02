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

package io.spine.base

import java.io.Serial

/**
 * A special kind of [RuntimeException] that represents an error, such as a programming or
 * a system configuration error made by a human or a software agent.
 *
 * Unlike [java.lang.Error], descendants of this class are meant to be caught.
 * Also, mistakes are going to be treated differently than other exceptions in
 * terms of catching, propagating, or logging.
 *
 * @param message The human-readable text with the details on the problem.
 * @param cause The cause of this mistake.
 */
public abstract class Mistake(message: String?, cause: Throwable?) :
    RuntimeException(message, cause) {

    /**
     * Creates an instance with an optional message.
     */
    public constructor(message: String?) : this(message, null)

    /**
     * Creates an instance with an optional cause.
     *
     * If the cause is provided its string form serves as a message.
     */
    public constructor(cause: Throwable?) : this(cause?.toString(), cause)

    /**
     * Creates an instance without a message or a cause.
     */
    public constructor() : this(null, null)

    public companion object {
        @Serial
        private const val serialVersionUID: Long = 0L
    }
}
