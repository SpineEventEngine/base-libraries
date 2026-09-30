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

import io.spine.annotation.VisibleForTesting;

import java.io.Serial;

import static java.lang.String.format;

/**
 * A stringifier for {@code enum} values.
 *
 * @param <E>
 *         the type of the {@code enum}
 */
final class EnumStringifier<E extends Enum<E>> extends SerializableStringifier<E> {

    @Serial
    private static final long serialVersionUID = 0L;

    private final Class<E> enumClass;

    EnumStringifier(Class<E> enumClass) {
        super(identity(enumClass));
        this.enumClass = enumClass;
    }

    @Override
    protected String toString(E e) {
        return e.toString();
    }

    @Override
    protected E fromString(String s) {
        var result = Enum.valueOf(enumClass, s);
        return result;
    }

    @VisibleForTesting
    static <E extends Enum<E>> String identity(Class<E> enumClass) {
        return format("Stringifiers.newForEnum(%s.class)", enumClass.getSimpleName());
    }
}
