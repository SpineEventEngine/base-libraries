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

import java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE
import java.lang.StackWalker.StackFrame
import java.util.stream.Stream

/**
 * Provides information about the class calling a method.
 */
internal object CallerProvider {

    private val stackWalker: StackWalker = StackWalker.getInstance(RETAIN_CLASS_REFERENCE)

    /**
     * Obtains the class of the object that calls the method from which
     * this method is being called.
     */
    fun callerClass(): Class<*> {
        return stackWalker.walk { frames ->
            frames.getCallingClass(skipFrames = 2)
        }
    }

    /**
     * Obtains the class preceding in the call chain the class that calls
     * the method from which this method is being called.
     */
    fun previousCallerClass(): Class<*> {
        return stackWalker.walk { frames ->
            frames.getCallingClass(skipFrames = 3)
        }
    }

    private fun Stream<StackFrame>.getCallingClass(skipFrames: Long) =
        skip(skipFrames)
            .findFirst()
            .map { frame -> frame.declaringClass }
            .get() // We're safe because the stacktrace will be deeper than 3.
}
