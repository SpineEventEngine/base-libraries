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

package io.spine.io

/**
 * Base interface for objects that may hold resources that need to be released
 * at the end of the object lifecycle.
 *
 * A class will benefit from implementing *this* interface instead of
 * [AutoCloseable] if it needs to see if the instance is [open][isOpen]
 * prior to making other calls.
 *
 * @see isOpen
 * @see checkOpen
 */
public interface Closeable : AutoCloseable {

    /**
     * Tells if the object is still open.
     *
     * Implementations must return `false` after [close] is invoked.
     */
    public val isOpen: Boolean

    /**
     * Performs the release of the resources held by this object.
     *
     * Overrides to remove the checked exception from the signature.
     *
     * Implementations <em>may</em> require that the object implementing this interface
     * invokes this method only once, and throw [IllegalStateException] for repeated invocation.
     * If this is the case, and you would like to avoid [pre-checking][isOpen] consider
     * using [closeIfOpen], which does the check itself.
     *
     * @see closeIfOpen
     */
    public override fun close()

    /**
     * Ensures that the object [isOpen].
     *
     * @throws IllegalStateException otherwise
     */
    @Throws(IllegalStateException::class)
    public fun checkOpen() {
        check(isOpen) { "`$this` is already closed." }
    }

    /**
     * Performs the release of the resources held by this object only if it is still open.
     *
     * Otherwise, does nothing.
     *
     * @see close
     */
    public fun closeIfOpen() {
        if (isOpen) {
            close()
        }
    }
}
