/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Converts the primitive and built-in types to the corresponding {@link Message}s and back.
 *
 * @param <M>
 *         the type of the message
 * @param <T>
 *         the type to convert to and from the message
 * @implNote The arguments are checked during the conversion and an
 *         {@link IllegalArgumentException} is thrown in case of mismatch. The type name used in
 *         the error message is a simple {@link Class#getName() Class.getName()} call result.
 *         It's the best-performant solution among options, such as
 *         {@link Class#getCanonicalName() Class.getCanonicalName()}.
 */
final class PrimitiveConverter<M extends Message, T> extends ProtoConverter<M, T> {

    private static final ImmutableMap<Class<?>, Converter<? extends Message, ?>>
            PROTO_WRAPPER_TO_CONVERTER =
            ImmutableMap.<Class<?>, Converter<? extends Message, ?>>builder()
                    .put(Int32Value.class, new Int32Converter())
                    .put(Int64Value.class, new Int64Converter())
                    .put(UInt32Value.class, new UInt32Converter())
                    .put(UInt64Value.class, new UInt64Converter())
                    .put(FloatValue.class, new FloatConverter())
                    .put(DoubleValue.class, new DoubleConverter())
                    .put(BoolValue.class, new BoolConverter())
                    .put(StringValue.class, new StringConverter())
                    .build();

    private static final ImmutableMap<Class<?>, Converter<? extends Message, ?>>
            PRIMITIVE_TO_CONVERTER =
            ImmutableMap.<Class<?>, Converter<? extends Message, ?>>builder()
                    .put(byte.class, new Int32Converter())
                    .put(Byte.class, new Int32Converter())
                    .put(short.class, new Int32Converter())
                    .put(Short.class, new Int32Converter())
                    .put(char.class, new Int32Converter())
                    .put(Character.class, new Int32Converter())
                    .put(int.class, new Int32Converter())
                    .put(Integer.class, new Int32Converter())
                    .put(long.class, new Int64Converter())
                    .put(Long.class, new Int64Converter())
                    .put(float.class, new FloatConverter())
                    .put(Float.class, new FloatConverter())
                    .put(double.class, new DoubleConverter())
                    .put(Double.class, new DoubleConverter())
                    .put(boolean.class, new BoolConverter())
                    .put(Boolean.class, new BoolConverter())
                    .put(String.class, new StringConverter())
                    .build();

    /**
     * Returns a set of Java primitive classes, which this converter can handle.
     */
    static ImmutableSet<Class<?>> supportedPrimitives() {
        return PRIMITIVE_TO_CONVERTER.keySet();
    }

    @Override
    protected T toObject(M input) {
        Class<?> boxedType = input.getClass();
        var converter = wrapperConverter(boxedType);
        var result = converter.convert(input);
        return requireNonNull(result);
    }

    @Override
    protected M toMessage(T input) {
        var cls = input.getClass();
        var converter = primitiveConverter(cls);
        var result = converter.convert(input);
        return requireNonNull(result);
    }

    private Converter<M, T> wrapperConverter(Class<?> boxedType) {
        @SuppressWarnings("unchecked")
        var converter = (Converter<M, T>) PROTO_WRAPPER_TO_CONVERTER.get(boxedType);
        checkArgument(
                converter != null,
                "Could not find a primitive type for `%s`.",
                boxedType.getName()
        );
        return converter;
    }

    private Converter<T, M> primitiveConverter(Class<?> cls) {
        @SuppressWarnings("unchecked")
        var converter = (Converter<M, T>) PRIMITIVE_TO_CONVERTER.get(cls);
        checkArgument(
                converter != null,
                "Could not find a wrapper type for `%s`.",
                cls.getName()
        );
        return converter.reverse();
    }

    private static final class Int32Converter extends WrappingConverter<Int32Value, Integer> {

        @Override
        protected Integer unwrap(Int32Value message) {
            return message.getValue();
        }

        @Override
        protected Int32Value wrap(Integer value) {
            return Int32Value.newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class Int64Converter extends WrappingConverter<Int64Value, Long> {

        @Override
        protected Long unwrap(Int64Value message) {
            return message.getValue();
        }

        @Override
        protected Int64Value wrap(Long value) {
            return Int64Value.newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class UInt32Converter extends WrappingConverter<UInt32Value, Integer> {

        @Override
        protected Integer unwrap(UInt32Value message) {
            return message.getValue();
        }

        @Override
        protected UInt32Value wrap(Integer value) {
            throw new UnsupportedOperationException(
                    "`Integer` value should be always mapped to a signed `Int32Value`. " +
                            "Conversion to `UInt32Value` is not supported."
            );
        }
    }

    private static final class UInt64Converter extends WrappingConverter<UInt64Value, Long> {

        @Override
        protected Long unwrap(UInt64Value message) {
            return message.getValue();
        }

        @Override
        protected UInt64Value wrap(Long value) {
            throw new UnsupportedOperationException(
                    "`Long` value should be always mapped to a signed `Int64Value`. " +
                            "Conversion to `UInt64Value` is not supported."
            );
        }
    }

    private static final class FloatConverter extends WrappingConverter<FloatValue, Float> {

        @Override
        protected Float unwrap(FloatValue message) {
            return message.getValue();
        }

        @Override
        protected FloatValue wrap(Float value) {
            return FloatValue.newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class DoubleConverter extends WrappingConverter<DoubleValue, Double> {

        @Override
        protected Double unwrap(DoubleValue message) {
            return message.getValue();
        }

        @Override
        protected DoubleValue wrap(Double value) {
            return DoubleValue.newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class BoolConverter extends WrappingConverter<BoolValue, Boolean> {

        @Override
        protected Boolean unwrap(BoolValue message) {
            return message.getValue();
        }

        @Override
        protected BoolValue wrap(Boolean value) {
            return BoolValue.newBuilder()
                    .setValue(value)
                    .build();
        }
    }

    private static final class StringConverter extends WrappingConverter<StringValue, String> {

        @Override
        protected String unwrap(StringValue message) {
            return message.getValue();
        }

        @Override
        protected StringValue wrap(String value) {
            return StringValue.newBuilder()
                    .setValue(value)
                    .build();
        }
    }
}
