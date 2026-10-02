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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.protobuf.Message;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static io.spine.protobuf.Messages.isMessageClass;
import static io.spine.string.Stringifiers.forBoolean;
import static io.spine.string.Stringifiers.forDuration;
import static io.spine.string.Stringifiers.forInteger;
import static io.spine.string.Stringifiers.forLong;
import static io.spine.string.Stringifiers.forString;
import static io.spine.string.Stringifiers.forTimestamp;
import static io.spine.string.Stringifiers.newForEnum;
import static io.spine.string.Stringifiers.newForMessage;
import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;

/**
 * The registry of converters of types to their string representations.
 */
public final class StringifierRegistry {

    private static final StringifierRegistry INSTANCE = new StringifierRegistry();

    private final Map<Type, Stringifier<?>> stringifiers = synchronizedMap(newHashMap());

    /**
     * Initializes the registry with the default stringifiers for primitive types,
     * {@link TimestampStringifier}, and {@link DurationStringifier}.
     */
    private StringifierRegistry() {
        register(
                forBoolean(),
                forInteger(),
                forLong(),
                forString(),
                forTimestamp(),
                forDuration()
        );
    }

    /**
     * Obtains the instance of the singleton registry.
     */
    public static StringifierRegistry instance() {
        return INSTANCE;
    }

    /**
     * Obtains a stringifier for the passed type.
     *
     * <p>If the passed type represents an enum, the returned instance is
     * {@linkplain Stringifiers#newForEnum(Class) adapted} to serve the type.
     *
     * <p>If the passed type is a message, the returned instance is adapted to the type
     * {@linkplain Stringifiers#newForMessage(Class) as well}.
     *
     * <p>This method does not serve {@code List} or {@link Map} types. In order to handle
     * such a stringification, please call corresponding methods of the {@link Stringifiers} class.
     *
     * @see Stringifiers#newForListOf(Class)
     * @see Stringifiers#newForMapOf(Class, Class)
     */
    static <T> Stringifier<T> getFor(Type typeOfT) {
        checkNotNull(typeOfT);
        Optional<Stringifier<T>> optional = instance().find(typeOfT);

        if (optional.isPresent()) {
            var stringifier = optional.get();
            return stringifier;
        }

        if (isEnumClass(typeOfT)) {
            @SuppressWarnings({"unchecked", "rawtypes"}) // OK since the type is checked above.
            var result = (Stringifier<T>) newForEnum((Class<Enum>) typeOfT);
            return result;
        }

        if (isMessageClass(typeOfT)) {
            @SuppressWarnings("unchecked") // OK since the type is checked above.
            var result = (Stringifier<T>) newForMessage((Class<Message>) typeOfT);
            return result;
        }

        var errMsg = format("No stringifier registered for the type: %s", typeOfT);
        throw new MissingStringifierException(errMsg);
    }

    /**
     * Casts the passed instance.
     *
     * <p>The cast is safe as we check the first type when
     * {@linkplain #register(Stringifier, Type) adding}.
     */
    @SuppressWarnings("unchecked")
    private static <T> Stringifier<T> cast(Stringifier<?> func) {
        return (Stringifier<T>) func;
    }

    /**
     * Registers the passed stringifier in the registry.
     *
     * @param stringifier
     *         the stringifier to register
     * @param typeOfT
     *         the value of the type of objects handled by the stringifier
     * @param <T>
     *         the type of the objects handled by the stringifier
     */
    public <T> void register(Stringifier<T> stringifier, Type typeOfT) {
        checkNotNull(typeOfT);
        checkNotNull(stringifier);
        stringifiers.put(typeOfT, stringifier);
    }

    /**
     * Registers the given stringifier with the registry.
     */
    public void register(Stringifier<?> stringifier) {
        checkNotNull(stringifier);
        var dataClass = getDataClass(stringifier.getClass());
        register(stringifier, dataClass);
    }

    /**
     * Registers the given stringifiers with the registry.
     */
    public void register(Stringifier<?> first, Stringifier<?>... more) {
        checkNotNull(first);
        checkNotNull(more);
        var list = ImmutableList.<Stringifier<?>>builder()
                .add(first)
                .addAll(Arrays.asList(more))
                .build();
        list.forEach(this::register);
    }

    /**
     * Obtains a {@code Stringifier} for the passed type.
     *
     * @param typeOfT
     *         the type to stringify
     * @param <T>
     *         the type of the values to convert
     * @return the found {@code Stringifier} or empty {@code Optional}
     */
    public <T> Optional<Stringifier<T>> find(Type typeOfT) {
        checkNotNull(typeOfT);
        @Nullable Stringifier<?> str = stringifiers.get(typeOfT);
        @Nullable Stringifier<T> result = str != null ? cast(str) : null;
        return Optional.ofNullable(result);
    }

    /**
     * Obtains the class handled by the passed class of stringifiers.
     */
    @SuppressWarnings("rawtypes")   /* Avoiding the generic hell. */
    private static Class<?> getDataClass(Class<? extends Stringifier> stringifierClass) {
        var supertypeToken = TypeToken.of(stringifierClass)
                                      .getSupertype(Stringifier.class);
        var genericSupertype = (ParameterizedType) supertypeToken.getType();
        var typeArguments = genericSupertype.getActualTypeArguments();
        var typeArgument = typeArguments[0];
        return (Class<?>) typeArgument;
    }

    private static boolean isEnumClass(Type type) {
        if (type instanceof Class) {
            var cls = (Class<?>) type;
            var isEnum = cls.isEnum();
            return isEnum;
        }
        return false;
    }
}
