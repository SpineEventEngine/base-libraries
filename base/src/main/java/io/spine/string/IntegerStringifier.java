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

import com.google.common.base.Converter;
import com.google.common.primitives.Ints;

/**
 * The {@code Stringifier} for the integer values.
 */
final class IntegerStringifier extends StringifierWithConverter<Integer> {

    private static final long serialVersionUID = 0L;

    private static final IntegerStringifier INSTANCE = new IntegerStringifier();

    private IntegerStringifier() {
        super("Stringifiers.forInteger()");
    }

    static IntegerStringifier getInstance() {
        return INSTANCE;
    }

    @Override
    protected Converter<String, Integer> converter() {
        return Ints.stringConverter();
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
