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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertHasPrivateParameterlessCtor;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static java.lang.reflect.Modifier.isFinal;

/**
 * The abstract base for stringifier tests.
 *
 * @param <T>
 *         the type of stringifier objects
 */
abstract class AbstractStringifierTest<T> {

    private final Stringifier<T> stringifier;
    private final Class<T> dataClass;

    AbstractStringifierTest(Stringifier<T> stringifier, Class<T> dataClass) {
        this.stringifier = stringifier;
        this.dataClass = dataClass;
    }

    protected abstract T createObject();

    static StringifierRegistry registry() {
        return StringifierRegistry.instance();
    }

    final Stringifier<T> stringifier() {
        return stringifier;
    }

    final Converter<String, T> parser() {
        return stringifier.reverse();
    }

    @Test
    @DisplayName("have private singleton constructor")
    void privateCtor() {
        assertHasPrivateParameterlessCtor(stringifier().getClass());
    }

    @Test
    @DisplayName("convert forward and backward")
    void convert() {
        var obj = createObject();

        final var str = stringifier.convert(obj);
        final var convertedBack = parser().convert(str);

        assertThat(convertedBack).isEqualTo(obj);
    }

    @Test
    @DisplayName("prohibit empty string input")
    void prohibitEmptyString() {
        assertIllegalArgument(() -> parser().convert(""));
    }

    @Test
    @DisplayName("serialize")
    void serialize() {
        var expected = stringifier();
        var stringifier = reserializeAndAssert(expected);
        assertThat(stringifier)
                .isSameInstanceAs(expected);
    }

    @Test
    @DisplayName("be registered")
    void isRegistered() {
        var found = registry().find(dataClass);
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("be a `final` class")
    void isFinalClass() {
        Class<?> stringifierClass = stringifier.getClass();
        assertThat(isFinal(stringifierClass.getModifiers()))
                .isTrue();
    }
}
