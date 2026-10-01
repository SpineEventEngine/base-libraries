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

package io.spine.string;

import java.io.Serial;
import java.io.Serializable;

/**
 * Abstract base serializable stringifiers.
 *
 * @param <T> the type to stringify
 */
public abstract class SerializableStringifier<T> extends Stringifier<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 0L;

    private final String identity;

    /**
     * Creates a new instance with the passed identity.
     *
     * @param identity the identity of the stringifier, which is used in {@link #toString()}.
     */
    protected SerializableStringifier(String identity) {
        super();
        this.identity = identity;
    }

    /**
     * Returns the identity of the stringifier.
     */
    @Override
    public final String toString() {
        return identity;
    }
}
