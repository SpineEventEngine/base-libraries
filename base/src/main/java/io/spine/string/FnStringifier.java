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

import io.spine.util.SerializableFunction;

import java.io.Serial;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base for stringifiers that convert values using function objects.
 *
 * @param <T>
 *         type of stringified objects
 */
@SuppressWarnings("AbstractClassNeverImplemented") /* Implemented in `base-types`. */
public abstract class FnStringifier<T> extends SerializableStringifier<T> {

    @Serial
    private static final long serialVersionUID = 0L;

    private final SerializableFunction<T, String> printer;
    private final SerializableFunction<String, T> parser;

    protected FnStringifier(String identity,
                            SerializableFunction<T, String> printer,
                            SerializableFunction<String, T> parser) {
        super(identity);
        this.printer = checkNotNull(printer);
        this.parser = checkNotNull(parser);
    }

    @Override
    protected final String toString(T obj) {
        var result = printer.apply(obj);
        return result;
    }

    @Override
    protected final T fromString(String s) {
        var result = parser.apply(s);
        return result;
    }
}
