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

/**
 * The {@code Stringifier} for the {@code String} values.
 *
 * <p>Always returns the original {@code String} passed as an argument.
 */
final class NoOpStringifier extends SerializableStringifier<String> {

    private static final long serialVersionUID = 0L;

    private static final NoOpStringifier INSTANCE = new NoOpStringifier();

    private NoOpStringifier() {
        super("Stringifiers.forString()");
    }

    static NoOpStringifier getInstance() {
        return INSTANCE;
    }

    @Override
    protected String toString(String obj) {
        return obj;
    }

    @Override
    protected String fromString(String s) {
        return s;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
