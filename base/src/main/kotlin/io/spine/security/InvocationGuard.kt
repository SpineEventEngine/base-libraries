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
        val msg = "The class `${callingClass.name}` is not allowed to perform this operation."
        return SecurityException(msg)
    }
}
