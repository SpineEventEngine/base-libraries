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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.EnumValue;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.AnyPacker.unpack;
import static java.util.Objects.requireNonNull;

/**
 * A utility for converting {@linkplain Message Protobuf Messages} (in the form of {@link Any})
 * to arbitrary {@linkplain Object Java Objects} and, where applicable, back to {@link Any}.
 *
 * <h2>Conversion of Singular Types</h2>
 *
 * <p>The following singular types can be converted in both directions:
 * <ul>
 *     <li>{@link Message} — converted via {@link AnyPacker}.
 *     <li>Java primitives — the passed {@link Any} is unpacked into one of the types
 *         {@code Int32Value, Int64Value, UInt32Value, UInt64Value, FloatValue, DoubleValue,
 *         BoolValue, StringValue, BytesValue} and then transformed into the corresponding Java
 *         type, either a primitive value, or {@code String} or {@link ByteString}. For more info,
 *         see <a href="https://developers.google.com/protocol-buffers/docs/proto3#scalar">
 *         the official document</a>.
 *     <li>{@linkplain Enum Java Enum} types — the passed {@link Any} is unpacked into the {@link
 *         EnumValue} type and then is converted to the Java Enum through the value {@linkplain
 *         EnumValue#getName() name} or {@linkplain EnumValue#getNumber() number}.
 * </ul>
 *
 * <p>An attempt to convert a singular type that is not listed above results in a
 * {@link UnsupportedOperationException}.
 *
 * <h2>Conversion of Collection Types</h2>
 *
 * <p>The following collection types can only be converted in one direction,
 * from {@linkplain Object Java Objects} to {@link Any}:
 * <ul>
 *     <li>{@link java.util.List Java List} types via {@link ListConverter}.
 *     <li>{@link java.util.Map Java Map} types via {@link MapConverter}.
 * </ul>
 *
 * <p>An attempt to convert a collection type that is not listed above, or to convert a supported
 * collection type back to a Java object, results in an {@link UnsupportedOperationException}.
 */
@Internal
public final class TypeConverter {

    private static final TypeUrl ENUM_VALUE_TYPE_URL = TypeUrl.of(EnumValue.class);

    /**
     * Prevents instantiation of this utility class.
     */
    private TypeConverter() {
    }

    /**
     * Converts the given {@link Any} value to a Java {@link Object}.
     *
     * @param message
     *         the {@link Any} value to convert
     * @param target
     *         the conversion target class
     * @param <T>
     *         the conversion target type
     * @return the converted value
     */
    public static <T> T toObject(Any message, Class<T> target) {
        checkNotNull(message);
        checkNotNull(target);
        checkNotRawEnum(message, target);
        Converter<? super Message, T> converter = ProtoConverter.forType(target);
        var genericMessage = unpack(message);
        var result = requireNonNull(converter.convert(genericMessage));
        return requireNonNull(result);
    }

    /**
     * Converts the given value to Protobuf {@link Any}.
     *
     * @param value
     *         the {@link Object} value to convert
     * @param <T>
     *         the converted object type
     * @return the packed value
     * @see #toMessage(Object)
     */
    public static <T> Any toAny(T value) {
        checkNotNull(value);
        var message = toMessage(value);
        var result = AnyPacker.pack(message);
        return result;
    }

    /**
     * Converts the given value to a corresponding Protobuf {@link Message} type.
     *
     * @param value
     *         the {@link Object} value to convert
     * @param <T>
     *         the converted object type
     * @return the wrapped value
     */
    public static <T> Message toMessage(T value) {
        @SuppressWarnings("unchecked" /* Must be checked at runtime. */)
        var srcClass = (Class<T>) value.getClass();
        var converter = ProtoConverter.forType(srcClass);
        var message = converter.reverse().convert(value);
        requireNonNull(message);
        return message;
    }

    /**
     * Converts the given value to a corresponding Protobuf {@link Message} type.
     *
     * <p>Unlike {@link #toMessage(Object)}, casts the message to the specified class.
     *
     * @param value
     *         the {@link Object} value to convert
     * @param <T>
     *         the converted object type
     * @param <M>
     *         the resulting message type
     * @return the wrapped value
     */
    public static <T, M extends Message> M toMessage(T value, Class<M> messageClass) {
        checkNotNull(messageClass);
        var message = toMessage(value);
        return messageClass.cast(message);
    }

    /**
     * Makes sure no incorrectly packed enum values are passed to the message converter.
     *
     * <p>Currently, the enum values can only be converted from the {@link EnumValue} proto type.
     * All other enum representations, including plain strings and numbers, are not supported.
     */
    private static void checkNotRawEnum(Any message, Class<?> target) {
        if (!target.isEnum()) {
            return;
        }
        var typeUrl = message.getTypeUrl();
        var enumValueTypeUrl = ENUM_VALUE_TYPE_URL.value();
        checkArgument(
                enumValueTypeUrl.equals(typeUrl),
                "Currently the conversion of enum types packed as `%s` is not supported. " +
                        "Please make sure the enum value is wrapped with `%s` on the calling site.",
                typeUrl, enumValueTypeUrl
        );
    }
}
