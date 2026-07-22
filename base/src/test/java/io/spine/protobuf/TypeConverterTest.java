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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.EnumValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import io.spine.test.protobuf.TaskStatus;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.protobuf.TypeConverter.toObject;
import static io.spine.protobuf.env.TypeConverterTestEnv.toProtoList;
import static io.spine.protobuf.env.TypeConverterTestEnv.toProtoMap;
import static io.spine.test.protobuf.TaskStatus.EXECUTING;
import static io.spine.test.protobuf.TaskStatus.FAILED;
import static io.spine.test.protobuf.TaskStatus.SUCCESS;
import static io.spine.test.protobuf.TaskStatus.UNRECOGNIZED;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`TypeConverter` utility class should")
class TypeConverterTest extends UtilityClassTest<TypeConverter> {

    TypeConverterTest() {
        super(TypeConverter.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(Any.class, Any.getDefaultInstance());
    }

    @Nested
    @DisplayName("map")
    class Map {

        @Test
        @DisplayName("arbitrary message to itself")
        void arbitraryMessageToItself() {
            Message message = StringValue.of(newUuid());
            checkMapping(message, message);
        }

        @Test
        @DisplayName("`Int32Value` to `int`")
        void int32ValueToInt() {
            var rawValue = 42;
            Message value = Int32Value.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`Int64Value` to `int`")
        void int64ValueToLong() {
            long rawValue = 42;
            Message value = Int64Value.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`FloatValue` to `float`")
        void floatValueToFloat() {
            var rawValue = 42.0f;
            Message value = FloatValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`DoubleValue` to `double`")
        void doubleValueToDouble() {
            var rawValue = 42.0;
            Message value = DoubleValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`BoolValue` to `boolean`")
        void boolValueToBoolean() {
            var rawValue = true;
            Message value = BoolValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`StringValue` to `String`")
        void stringValueToString() {
            var rawValue = "Hello";
            Message value = StringValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`BytesValue` to `ByteString`")
        void bytesValueToByteString() {
            var rawValue = ByteString.copyFrom("Hello!", UTF_8);
            Message value = BytesValue.of(rawValue);
            checkMapping(rawValue, value);
        }

        @Test
        @DisplayName("`UInt32` to `int`")
        void uint32ToInt() {
            var value = 42;
            var wrapped = UInt32Value.of(value);
            var packed = AnyPacker.pack(wrapped);
            int mapped = TypeConverter.toObject(packed, Integer.class);
            assertEquals(value, mapped);
        }

        @Test
        @DisplayName("`UInt64` to `int`")
        void uint64ToLong() {
            var value = 42L;
            var wrapped = UInt64Value.of(value);
            var packed = AnyPacker.pack(wrapped);
            long mapped = TypeConverter.toObject(packed, Long.class);
            assertEquals(value, mapped);
        }

        private void checkMapping(Object javaObject, Message protoObject) {
            var wrapped = AnyPacker.pack(protoObject);
            var mappedJavaObject = TypeConverter.toObject(wrapped, javaObject.getClass());
            assertEquals(javaObject, mappedJavaObject);
            var restoredWrapped = TypeConverter.toAny(mappedJavaObject);
            var restored = AnyPacker.unpack(restoredWrapped);
            assertEquals(protoObject, restored);
        }
    }

    @Nested
    @DisplayName("convert `EnumValue` to `Enum`")
    class ConvertEnumValueToEnum {

        @Test
        @DisplayName("if the `EnumValue` has a constant name specified")
        void ifHasName() {
            var value = EnumValue.newBuilder()
                    .setName(SUCCESS.name())
                    .build();
            checkConverts(value, SUCCESS);
        }

        @Test
        @DisplayName("if the `EnumValue` has a constant number specified")
        void ifHasNumber() {
            var value = EnumValue.newBuilder()
                    .setNumber(EXECUTING.getNumber())
                    .build();
            checkConverts(value, EXECUTING);
        }

        @Test
        @DisplayName("with `UNRECOGNIZED` value by name")
        void unrecognizedByName() {
            var value = EnumValue.newBuilder()
                    .setName(UNRECOGNIZED.name())
                    .build();
            checkConverts(value, UNRECOGNIZED);
        }

        @Test
        @DisplayName("with `UNRECOGNIZED` value by number `-1`")
        void unrecognizedByNumber() {
            var value = EnumValue.newBuilder()
                    .setNumber(-1)
                    .build();
            checkConverts(value, UNRECOGNIZED);
        }

        @Test
        @DisplayName("using the constant name if both the name and the number are specified")
        void preferringConversionWithName() {
            // Set the different name and number just for the sake of test.
            var value = EnumValue.newBuilder()
                    .setName(SUCCESS.name())
                    .setNumber(FAILED.getNumber())
                    .build();
            checkConverts(value, SUCCESS);
        }

        private void checkConverts(EnumValue enumValue, Enum<?> expected) {
            var wrapped = AnyPacker.pack(enumValue);
            var mappedJavaObject = TypeConverter.toObject(wrapped, expected.getDeclaringClass());
            assertEquals(expected, mappedJavaObject);
        }
    }

    @Nested
    @DisplayName("throw an `IAE` when")
    @SuppressWarnings("CheckReturnValue") // The method is called to throw an exception.
    class ThrowIAEOnEnumConversion {

        @Test
        @DisplayName("the `EnumValue` with an unknown name is passed")
        void unknownName() {
            var unknownName = "some_name";
            var value = EnumValue.newBuilder()
                    .setName(unknownName)
                    .build();
            var wrapped = AnyPacker.pack(value);
            assertIllegalArgument(() -> toObject(wrapped, TaskStatus.class));
        }

        @Test
        @DisplayName("the `EnumValue` with an unknown number is passed")
        void unknownNumber() {
            var unknownValue = 156;
            var value = EnumValue.newBuilder()
                    .setNumber(unknownValue)
                    .build();
            var wrapped = AnyPacker.pack(value);
            assertIllegalArgument(() -> toObject(wrapped, TaskStatus.class));
        }

        @Test
        @DisplayName("converting a non-`EnumValue` object to a `Enum`")
        void rawValuesForEnum() {
            var enumNumber = Int32Value.newBuilder()
                    .setValue(SUCCESS.getNumber())
                    .build();
            var packed = AnyPacker.pack(enumNumber);
            assertIllegalArgument(() -> toObject(packed, TaskStatus.class));
        }
    }

    @Nested
    @DisplayName("convert")
    class Convert {

        @Test
        @DisplayName("`List` to `Any`")
        void listToAny() {
            var list = List.of("s1", "s2", "s3");
            var protoList = toProtoList(list);
            assertEquals(protoList, AnyPacker.unpack(TypeConverter.toAny(list)));
        }

        @Test
        @DisplayName("`Map` to `Any`")
        void mapToAny() {
            var map = java.util.Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            var protoMap = toProtoMap(map);
            assertEquals(protoMap, AnyPacker.unpack(TypeConverter.toAny(map)));
        }

        @Test
        @DisplayName("`Enum` to `EnumValue`")
        void convertEnumToEnumValue() {
            var restoredWrapped = TypeConverter.toAny(SUCCESS);
            var restored = AnyPacker.unpack(restoredWrapped);
            var expected = EnumValue.newBuilder()
                    .setName(SUCCESS.name())
                    .setNumber(SUCCESS.getNumber())
                    .build();
            assertEquals(expected, restored);
        }

        @Test
        @DisplayName("a value to a particular message")
        void valueToParticularMessage() {
            var stringValue = "a string value";
            var convertedValue = toMessage(stringValue, StringValue.class);
            assertEquals(stringValue, convertedValue.getValue());
        }
    }

    @Nested
    @DisplayName("throw `UnsupportedOperationException` when")
    class ThrowUnsupportedOperationWhen {

        @Test
        @DisplayName("converting an unsupported type")
        void convertingUnsupportedType() {
            var dataTime = LocalDateTime.now(ZoneId.systemDefault());
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> TypeConverter.toAny(dataTime)
            );
        }

        @Test
        @DisplayName("converting `Any` to `List`")
        void convertingAnyToList() {
            var protoList = toProtoList(List.of("s1", "s2", "s3"));
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> TypeConverter.toObject(AnyPacker.pack(protoList), List.class)
            );
        }

        @Test
        @DisplayName("converting `Any` to `Map`")
        void convertingAnyToMap() {
            var map =  java.util.Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            var protoMap = toProtoMap(map);
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> TypeConverter.toObject(AnyPacker.pack(protoMap), Map.class)
            );
        }
    }
}
