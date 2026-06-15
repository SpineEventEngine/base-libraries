/*
 * Copyright 2024, TeamDev. All rights reserved.
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
