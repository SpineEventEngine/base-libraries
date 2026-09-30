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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code Stringifier} for boolean values.
 */
final class BooleanStringifier extends SerializableStringifier<Boolean> {

    private static final long serialVersionUID = 0L;

    private static final BooleanStringifier INSTANCE = new BooleanStringifier();

    private BooleanStringifier() {
        super("Stringifiers.forBoolean()");
    }

    static BooleanStringifier getInstance() {
        return INSTANCE;
    }

    @Override
    protected String toString(Boolean value) {
        checkNotNull(value);
        return value.toString();
    }

    @Override
    protected Boolean fromString(String s) {
        checkNotNull(s);
        Boolean result = Boolean.parseBoolean(s);
        return result;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
