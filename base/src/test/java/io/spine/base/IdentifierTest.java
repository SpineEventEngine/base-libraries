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

package io.spine.base;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.OptionalSubject;
import com.google.protobuf.Any;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import io.spine.protobuf.AnyPacker;
import io.spine.test.identifiers.EnumFieldId;
import io.spine.test.identifiers.IdWithPrimitiveFields;
import io.spine.test.identifiers.NestedMessageId;
import io.spine.test.identifiers.SeveralFieldsId;
import io.spine.test.identifiers.TaskStatus;
import io.spine.test.identifiers.TimestampFieldId;
import io.spine.test.identifiers.TwoEnumFieldsId;
import io.spine.testing.TestValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64;
import static io.spine.base.IdType.ENUM;
import static io.spine.base.IdType.INTEGER;
import static io.spine.base.IdType.LONG;
import static io.spine.base.IdType.MESSAGE;
import static io.spine.base.IdType.STRING;
import static io.spine.base.Identifier.EMPTY_ID;
import static io.spine.base.Identifier.NULL_ID;
import static io.spine.base.Identifier.checkSupported;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Identifier` should")
class IdentifierTest {

    private static final String TEST_ID = "someTestId 1234567890 !@#$%^&()[]{}-+=_";

    @Nested
    @DisplayName("generate a new ID value which is")
    class GenerateUuid {

        @Test
        @DisplayName("UUID-based")
        void uuidBased() {
            // We have non-empty values.
            assertTrue(newUuid().length() > 0);
        }

        @Test
        @DisplayName("unique")
        void unique() {
            assertNotEquals(newUuid(), newUuid());
        }
    }

    @Nested
    @DisplayName("create values by type")
    class CreateByType {

        @Test
        @DisplayName("`String`")
        void ofString() {
            assertTypeOf("", STRING);
        }

        @Test
        @DisplayName("`Integer`")
        void ofInteger() {
            assertTypeOf(0, INTEGER);
        }

        @Test
        @DisplayName("`Long`")
        void ofLong() {
            assertTypeOf(0L, LONG);
        }

        @Test
        @DisplayName("`Message`")
        void ofMessage() {
            assertTypeOf(toMessage(300), MESSAGE);
        }

        @Test
        @DisplayName("a Protobuf enum")
        void ofEnum() {
            assertTypeOf(TaskStatus.TASK_OPEN, ENUM);
        }

        private static void assertTypeOf(Object id, IdType expectedType) {
            Identifier<?> identifier = Identifier.from(id);
            assertThat(identifier.type()).isEqualTo(expectedType);
        }
    }

    @Nested
    @DisplayName("recognize type by supported `Message` type")
    class RecognizeType {

        @Test
        @DisplayName("`INTEGER`")
        void ofInteger() {
            assertTrue(INTEGER.matchMessage(toMessage(10)));
        }

        @Test
        @DisplayName("`LONG`")
        void ofLong() {
            assertTrue(LONG.matchMessage(toMessage(1020L)));
        }

        @Test
        @DisplayName("`STRING`")
        void ofString() {
            assertTrue(STRING.matchMessage(toMessage("")));
        }

        @Test
        @DisplayName("`MESSAGE`")
        void ofMessage() {
            assertTrue(MESSAGE.matchMessage(Timestamp.getDefaultInstance()));

            // Do not consider primitive type wrappers as message types.
            assertFalse(MESSAGE.matchMessage(StringValue.getDefaultInstance()));
            assertFalse(MESSAGE.matchMessage(Int32Value.getDefaultInstance()));
            assertFalse(MESSAGE.matchMessage(Int64Value.getDefaultInstance()));
        }
    }

    @Nested
    @DisplayName("obtain default value for")
    class DefaultValue {

        @Test
        @DisplayName("`Integer`")
        void ofInteger() {
            assertThat(Identifier.defaultValue(Integer.class))
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("`Long`")
        void ofLong() {
            assertThat(Identifier.defaultValue(Long.class))
                    .isEqualTo(0L);
        }

        @Test
        @DisplayName("`String`")
        void ofString() {
            assertThat(Identifier.defaultValue(String.class))
                    .isEmpty();
        }

        @Test
        @DisplayName("`Message`")
        void ofMessage() {
            assertThat(Identifier.defaultValue(Timestamp.class))
                    .isEqualTo(Timestamp.getDefaultInstance());
        }

        @Test
        @DisplayName("a Protobuf enum")
        void ofEnum() {
            assertThat(Identifier.defaultValue(TaskStatus.class))
                    .isEqualTo(TaskStatus.TASK_STATUS_UNDEFINED);
        }
    }

    @Nested
    @DisplayName("return `EMPTY_ID` when converting")
    class EmptyId {

        @Test
        @DisplayName("empty string")
        void emptyString() {
            assertEmpty("");
        }

        @Test
        @DisplayName("a `Message` with the default value")
        void defaultInstance() {
            assertEmpty(StringValue.getDefaultInstance());
        }

        @Test
        @DisplayName("a `Message` with a field of `Message` type, which has the default value")
        void defaultNestedMessage() {
            assertEmpty(TimestampFieldId.getDefaultInstance());
        }

        private void assertEmpty(Object id) {
            var str = Identifier.toString(id);
            assertThat(str).isEqualTo(EMPTY_ID);
        }
    }

    @Nested
    @DisplayName("verify if ID is empty")
    class VerifyEmptyId {

        @Nested
        @DisplayName("returning always `false` for")
        class AlwaysNonEmpty {

            @Test
            @DisplayName("`Integer`")
            void intValue() {
                assertNotEmpty(0);
                assertNotEmpty(TestValues.random(Integer.MIN_VALUE, Integer.MAX_VALUE));
            }

            @Test
            @DisplayName("`Long`")
            void longValue() {
                assertNotEmpty(0L);
                assertNotEmpty(TestValues.longRandom(Long.MIN_VALUE, Long.MAX_VALUE));
            }
        }

        @Test
        @DisplayName("taking string value")
        void emptyString() {
            assertEmpty("");
            assertNotEmpty(TestValues.randomString());
        }

        @Test
        @DisplayName("taking message fields")
        void messageId() {
            assertEmpty(Struct.getDefaultInstance());
            assertNotEmpty(Time.currentTime());
        }

        @Test
        @DisplayName("treating the zero-numbered enum constant as empty")
        void enumId() {
            assertEmpty(TaskStatus.TASK_STATUS_UNDEFINED);
            assertNotEmpty(TaskStatus.TASK_OPEN);
            assertNotEmpty(TaskStatus.TASK_CLOSED);
        }

        <I> void assertNotEmpty(I value) {
            assertThatEmpty(value).isFalse();
        }

        <I> void assertEmpty(I value) {
            assertThatEmpty(value).isTrue();
        }

        private <I> BooleanSubject assertThatEmpty(I value) {
            return assertThat(Identifier.isEmpty(value));
        }
    }

    @Nested
    @DisplayName("convert to `String` an ID of type")
    class ConvertToString {

        @Test
        @DisplayName("`Integer`")
        @SuppressWarnings("UnnecessaryBoxing")
            // OK as we want to show types explicitly.
        void ofInteger() {
            assertEquals("10", Identifier.toString(Integer.valueOf(10)));
        }

        @Test
        @DisplayName("`Long`")
        @SuppressWarnings("UnnecessaryBoxing")
            // OK as we want to show types explicitly.
        void ofLong() {
            assertEquals("100000", Identifier.toString(Long.valueOf(100_000)));
        }

        @Test
        @DisplayName("wrapped `Integer`")
        void ofWrappedInteger() {
            var value = 1024;
            var id = Int32Value.of(value);
            var expected = Integer.toString(value);

            var actual = Identifier.toString(id);

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("wrapped `Long`")
        void ofWrappedLong() {
            var value = 100500L;
            var id = Int64Value.of(value);
            var expected = Long.toString(value);

            var actual = Identifier.toString(id);

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("`String`")
        void ofString() {
            assertEquals(TEST_ID, Identifier.toString(TEST_ID));
        }

        @Test
        @DisplayName("wrapped `String`")
        void ofWrappedString() {
            var id = StringValue.of(TEST_ID);

            var result = Identifier.toString(id);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("`Message` with string field")
        void ofMessage() {
            var id = StringValue.of(TEST_ID);

            var result = Identifier.toString(id);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("a Protobuf enum")
        void ofEnum() {
            assertEquals("TASK_OPEN", Identifier.toString(TaskStatus.TASK_OPEN));
        }

        @Test
        @DisplayName("`Message` with nested `Message`")
        void ofNestedMessage() {
            var value = StringValue.of(TEST_ID);
            var idToConvert = NestedMessageId.newBuilder()
                    .setId(value)
                    .build();

            var result = Identifier.toString(idToConvert);

            assertEquals(TEST_ID, result);
        }

        @Test
        @DisplayName("`Any`")
        void ofAny() {
            var messageToWrap = StringValue.of(TEST_ID);
            var any = AnyPacker.pack(messageToWrap);

            var result = Identifier.toString(any);

            assertEquals(TEST_ID, result);
        }
    }

    @Test
    @DisplayName("provide string conversion of messages with several fields")
    void toStringSeveralFields() {
        var nestedString = "nested_string";
        var outerString = "outer_string";
        var number = 256;

        var nestedMessageString = StringValue.of(nestedString);
        var idToConvert = SeveralFieldsId.newBuilder()
                .setString(outerString)
                .setNumber(number)
                .setMessage(nestedMessageString)
                .build();

        var expected = "string=\"" + outerString + '\"' +
                " number=" + number +
                " message { value=\"" + nestedString + "\" }";

        var actual = Identifier.toString(idToConvert);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester()
                .setDefault(Any.class, AnyPacker.pack(StringValue.of(TEST_ID)))
                .setDefault(Descriptor.class, Any.getDescriptor())
                .testAllPublicStaticMethods(Identifier.class);
    }

    @Nested
    @DisplayName("create values depend on the wrapper message type for")
    @SuppressWarnings("BadImport") // OK to static-import `Identifier.Type` for brevity.
    class CreateValues {

        @Test
        @DisplayName("`int`")
        void intValue() {
            assertEquals(10, INTEGER.fromMessage(toMessage(10)));
        }

        @Test
        @DisplayName("`long`")
        void longValue() {
            assertEquals(1024L, LONG.fromMessage(toMessage(1024L)));
        }

        @Test
        @DisplayName("`String`")
        void stringValue() {
            var value = getClass().getSimpleName();
            assertEquals(value, STRING.fromMessage(toMessage(value)));
        }
    }

    @Nested
    @DisplayName("check that a class of identifiers is supported for")
    class CheckSupported {

        @Test
        @DisplayName("`String`")
        void stringValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(String.class));
        }

        @Test
        @DisplayName("`Integer`")
        void integerValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(Integer.class));
        }

        @Test
        @DisplayName("`Long`")
        void longValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(Long.class));
        }

        @Test
        @DisplayName("a `Message` class")
        void messageValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(StringValue.class));
        }

        @Test
        @DisplayName("a Protobuf enum class")
        void enumValue() {
            assertDoesNotThrow(() -> Identifier.checkSupported(TaskStatus.class));
        }
    }

    @Test
    @DisplayName("throw `IllegalArgumentException` for unsupported class")
    void checkNotSupported() {
        assertIllegalArgument(() -> checkSupported(Boolean.class));
    }

    @Nested
    @DisplayName("tell if a Protobuf field type is supported for an ID")
    class SupportedIdType {

        private final ImmutableList<FieldDescriptorProto.Type> supported = ImmutableList.of(
                TYPE_STRING,
                TYPE_INT32, TYPE_UINT32, TYPE_SINT32, TYPE_FIXED32, TYPE_SFIXED32,
                TYPE_INT64, TYPE_UINT64, TYPE_SINT64, TYPE_FIXED64, TYPE_SFIXED64,
                TYPE_ENUM,
                TYPE_MESSAGE);

        private final ImmutableList<FieldDescriptorProto.Type> unsupported = ImmutableList.of(
                TYPE_BOOL, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BYTES, TYPE_GROUP);

        @Test
        @DisplayName("accepting `string`, integer, `enum`, and `Message` types")
        void accepting() {
            for (var type : supported) {
                assertWithMessage("`%s` should be supported", type)
                        .that(Identifier.isSupportedIdType(type))
                        .isTrue();
            }
        }

        @Test
        @DisplayName("rejecting `bool`, `float`, `double`, `bytes`, and `group` types")
        void rejecting() {
            for (var type : unsupported) {
                assertWithMessage("`%s` should not be supported", type)
                        .that(Identifier.isSupportedIdType(type))
                        .isFalse();
            }
        }

        @Test
        @DisplayName("classifying every declared field type")
        void everyType() {
            // `FieldDescriptorProto.Type` comes from the proto2 `descriptor.proto`,
            // so it has no `UNRECOGNIZED` sentinel — every declared value must be classified.
            for (var type : FieldDescriptorProto.Type.values()) {
                var classified = supported.contains(type) || unsupported.contains(type);
                assertWithMessage("`%s` must be classified as supported or not", type)
                        .that(classified)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("not classifying any type as both supported and unsupported")
        void noOverlap() {
            for (var type : supported) {
                assertThat(unsupported).doesNotContain(type);
            }
        }

        @Test
        @DisplayName("accepting a supported field descriptor")
        void supportedField() {
            assertSupported(stringField(), true);
            assertSupported(intField(), true);
            assertSupported(longField(), true);
            assertSupported(messageField(), true);
            assertSupported(enumField(), true);
        }

        @Test
        @DisplayName("rejecting an unsupported field descriptor")
        void unsupportedField() {
            assertSupported(boolField(), false);
        }

        @Test
        @DisplayName("agreeing with `IdType.matchField` for each field")
        void consistentWithMatchField() {
            var fields = ImmutableList.of(
                    stringField(), intField(), longField(),
                    messageField(), enumField(), boolField());
            for (var field : fields) {
                var anyIdTypeMatches = stream(IdType.values()).anyMatch(t -> t.matchField(field));
                assertWithMessage("Disagreement for field `%s`", field.getFullName())
                        .that(Identifier.isSupportedIdType(field))
                        .isEqualTo(anyIdTypeMatches);
            }
        }

        private void assertSupported(FieldDescriptor field, boolean expected) {
            assertWithMessage("`%s`", field.getFullName())
                    .that(Identifier.isSupportedIdType(field))
                    .isEqualTo(expected);
        }

        private FieldDescriptor severalFieldsField(int index) {
            return SeveralFieldsId.getDescriptor()
                                  .getFields()
                                  .get(index);
        }

        private FieldDescriptor stringField() {
            return severalFieldsField(0);
        }

        private FieldDescriptor intField() {
            return severalFieldsField(1);
        }

        private FieldDescriptor messageField() {
            return severalFieldsField(2);
        }

        private FieldDescriptor longField() {
            return severalFieldsField(3);
        }

        private FieldDescriptor enumField() {
            return EnumFieldId.getDescriptor()
                              .getFields()
                              .get(0);
        }

        private FieldDescriptor boolField() {
            return IdWithPrimitiveFields.getDescriptor()
                                        .getFields()
                                        .get(2);
        }
    }

    @Nested
    @DisplayName("reject unsupported")
    class RejectUnsupported {

        @Test
        @DisplayName("value")
        @SuppressWarnings("UnnecessaryBoxing")
            // We want to make the unsupported type obvious.
        void value() {
            assertIllegalArgument(() -> Identifier.toString(Boolean.valueOf(true)));
        }

        @Test
        @DisplayName("class")
        void clazz() {
            assertIllegalArgument(() -> Identifier.toType(Float.class));
        }
    }

    @Nested
    @DisplayName("unpack")
    class Unpack {

        @Test
        @DisplayName("Any with StringValue and cast")
        void anyWithStringValue() {
            var testIdMessage = StringValue.of(TEST_ID);
            var any = AnyPacker.pack(testIdMessage);
            var unpackedId = Identifier.unpack(any, String.class);
            assertEquals(testIdMessage.getValue(), unpackedId);
        }

        @Test
        @DisplayName("and throw if `Any` is empty")
        void rejectEmptyAny() {
            assertIllegalArgument(() -> Identifier.unpack(Any.getDefaultInstance()));
        }

        @Test
        @DisplayName("a Protobuf enum packed into `Any` using the target class")
        void enumRoundTrip() {
            var any = Identifier.pack(TaskStatus.TASK_OPEN);
            var unpacked = Identifier.unpack(any, TaskStatus.class);
            assertEquals(TaskStatus.TASK_OPEN, unpacked);
        }
    }

    @Test
    @DisplayName("reject packing unsupported type")
    @SuppressWarnings("UnnecessaryBoxing")
        // We want to make the unsupported type obvious.
    void noPackingForUnsupported() {
        assertIllegalArgument(() -> Identifier.pack(Boolean.valueOf(false)));
    }

    @Test
    @DisplayName("return `NULL_ID` when converting null")
    void nullId() {
        assertEquals(NULL_ID, Identifier.toString(null));
    }

    @Nested
    @DisplayName("recognize field descriptor")
    @SuppressWarnings("BadImport") // OK to static-import `Identifier.Type` for brefity.
    class FieldDescr {

        @Test
        @DisplayName("`Integer`")
        void intField() {
            assertTrue(INTEGER.matchField(field(1)));
        }

        @Test
        @DisplayName("`Long`")
        void longField() {
            assertTrue(LONG.matchField(field(3)));
        }

        @Test
        @DisplayName("`String`")
        void stringField() {
            assertTrue(STRING.matchField(field(0)));
        }

        @Test
        @DisplayName("`Message`")
        void messgeField() {
            assertTrue(MESSAGE.matchField(field(2)));
        }

        @Test
        @DisplayName("a Protobuf enum")
        void enumField() {
            var field = EnumFieldId.getDescriptor()
                                   .getFields()
                                   .get(0);
            assertTrue(ENUM.matchField(field));
        }

        FieldDescriptor field(int index) {
            var field =
                    SeveralFieldsId.getDescriptor()
                                   .getFields()
                                   .get(index);
            return field;
        }
    }

    @Nested
    @DisplayName("find field by type")
    class FindingField {

        @Test
        @DisplayName("`Integer`")
        void intType() {
            assertFound(Integer.class, SeveralFieldsId.getDescriptor());
            assertNotFound(Integer.class, TimestampFieldId.getDescriptor());
        }

        @Test
        @DisplayName("`Long`")
        void longType() {
            assertFound(Long.class, SeveralFieldsId.getDescriptor());
            assertNotFound(Long.class, TimestampFieldId.getDescriptor());
        }

        @Test
        @DisplayName("`String`")
        void stringType() {
            assertFound(String.class, SeveralFieldsId.getDescriptor());
            assertNotFound(String.class, NestedMessageId.getDescriptor());
        }

        @Test
        @DisplayName("`Message`")
        void messageType() {
            assertFound(StringValue.class, SeveralFieldsId.getDescriptor());
            assertNotFound(StringValue.class, IdWithPrimitiveFields.getDescriptor());
        }

        @Test
        @DisplayName("a Protobuf enum")
        void enumType() {
            assertFound(TaskStatus.class, EnumFieldId.getDescriptor());
            assertNotFound(TaskStatus.class, IdWithPrimitiveFields.getDescriptor());
        }

        @Test
        @DisplayName("a Protobuf enum, by its specific enum type")
        void enumTypeDisambiguated() {
            // `TwoEnumFieldsId` declares `Priority priority = 1` before `TaskStatus status = 2`.
            // Searching for a `TaskStatus` ID must return the `status` field, not the first
            // enum field of an unrelated enum type.
            var found = Field.findIdField(TaskStatus.class, TwoEnumFieldsId.getDescriptor());
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("status");
        }

        <I> void assertFound(Class<I> idClass, Descriptor message) {
            assertField(idClass, message).isPresent();
        }

        <I> void assertNotFound(Class<I> idClass, Descriptor message) {
            assertField(idClass, message).isEmpty();
        }

        private static <I> OptionalSubject assertField(Class<I> idClass, Descriptor message) {
            return assertThat(Field.findIdField(idClass, message));
        }
    }

    @Test
    @DisplayName("allow event `Empty` as a `Message`-based ID")
    void throwOnUnpacking() {
        var packedEmpty = AnyPacker.pack(Empty.getDefaultInstance());

        assertThat(Identifier.unpack(packedEmpty))
                .isEqualTo(Empty.getDefaultInstance());
    }
}
