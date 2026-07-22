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

package io.spine.security

import io.spine.security.CallerProvider.previousCallerClass
import org.checkerframework.checker.signature.qual.ClassGetName
import org.checkerframework.checker.signature.qual.FullyQualifiedName

/**
 * Controls which class can call a method.
 */
public object InvocationGuard {

    /**
     * Throws [SecurityException] if the calling class is not that passed.
     */
    @JvmStatic
    public fun allowOnly(allowedCallerClass: @FullyQualifiedName String) {
        val callingClass = previousCallerClass()
        if (allowedCallerClass != callingClass.name) {
            throw nonAllowedCaller(callingClass)
        }
    }

    /**
     * Throws [SecurityException] if the calling class is not among the named.
     */
    @JvmStatic
    public fun allowOnly(
        firstClass: @FullyQualifiedName String,
        vararg otherClasses: String
    ) {
        val callingClass = previousCallerClass()
        val allowedCallers = buildSet {
            add(firstClass)
            addAll(otherClasses)
        }
        if (!allowedCallers.contains(callingClass.name)) {
            throw nonAllowedCaller(callingClass)
        }
    }

    private fun nonAllowedCaller(callingClass: @ClassGetName Class<*>): SecurityException {
        val msg = "The class `$callingClass.name` is not allowed to perform this operation."
        throw SecurityException(msg)
    }
}
