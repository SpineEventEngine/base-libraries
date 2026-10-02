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

package io.spine.base;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.test.protobuf.AnyHolder;
import io.spine.test.protobuf.GenericHolder;
import io.spine.test.protobuf.GenericHolder.Count;
import io.spine.test.protobuf.StringHolder;
import io.spine.test.protobuf.StringHolderHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Field.nameOf;
import static io.spine.base.Field.named;
import static io.spine.base.Field.parse;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.Assertions.assertNpe;

/**
 * Tests for {@link Field}.
 *
 * <p>See {@code field_paths_test.proto} for definition of proto types used in these tests.
 */
@DisplayName("`Field` should")
class FieldTest {

    @Test
    @DisplayName("pass null tolerance check")
    void nullCheck() {
        new NullPointerTester()
                .setDefault(Descriptor.class, Empty.getDescriptor())
                .testAllPublicStaticMethods(Field.class);
    }


    @Nested
    @DisplayName("parse the passed path")
    class Parsing {

        @Test
        @DisplayName("with immediate field name")
        void immediate() {
            var expected = "val";
            var field = Field.parse(expected);
            assertThat(field.path().getFieldNameList())
                    .containsExactly(expected);
        }

        @Test
        @DisplayName("delimited with dots")
        void nested() {
            var path = "highway.to.hell";
            var field = Field.parse(path);
            assertThat(field.toString())
                    .isEqualTo(path);
            assertThat(field.path().getFieldNameList())
                    .containsExactly("highway", "to", "hell");
        }

        @Test
        @DisplayName("rejecting empty path")
        void rejectingEmpty() {
            assertIllegalArgument(() -> parse(""));
        }
    }

    @Test
    @DisplayName("create the instance by the path")
    void byPath() {
        var expected = Field.doParse("road_to.mandalay");
        assertThat(Field.withPath(expected).path())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("create the field by its name")
    class SingleField {

        @Test
        @DisplayName("accepting non-empty string")
        void byName() {
            var name = "my_way";
            assertThat(Field.named(name).toString())
                    .isEqualTo(name);
        }

        @Test
        @DisplayName("rejecting `.` in the field name")
        void noSeparatorInName() {
            assertRejects("with.dot");
        }

        @Test
        @DisplayName("rejecting empty, null or blank names")
        void rejectEmpty() {
            assertRejects("");
            assertRejects(" ");
            assertRejects("  ");
            assertNpe(() -> named(null));
        }

        void assertRejects(String illegalValue) {
            assertIllegalArgument(() -> named(illegalValue));
        }
    }

    @Nested
    @DisplayName("obtain the value in a message")
    class GettingValue {

        private final Field field = Field.parse("any.val.type_url");
        private final Field missingField = Field.parse("type_url");
        private Message message;
        private Object expectedValue;

        @BeforeEach
        void createMessage() {
            var value = pack(Time.currentTime());
            expectedValue = value.getTypeUrl();
            var anyHolder = AnyHolder
                    .newBuilder()
                    .setVal(value)
                    .build();
            message = GenericHolder
                    .newBuilder()
                    .setAny(anyHolder)
                    .build();
        }

        @Test
        @DisplayName("as `Optional` value")
        void ifFound() {
            assertThat(field.findValue(message))
                  .hasValue(expectedValue);
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            assertThat(missingField.findValue(message))
                  .isEmpty();
        }

        @Test
        @DisplayName("directly via a nested path")
        void directValue() {
            assertThat(field.valueIn(message))
                    .isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("directly via a simple path")
        void obtainSimple() {
            var holder = StringHolder.newBuilder()
                    .setVal("foobar")
                    .build();
            var field = Field.parse("val");
            assertThat(field.valueIn(holder))
                    .isEqualTo(holder.getVal());
        }

        @Test
        @DisplayName("via recursive path")
        void recursivePath() {
            var value = "42";
            var holder0 = StringHolder.newBuilder()
                    .setVal(value)
                    .build();
            var holder1 = StringHolderHolder.newBuilder()
                    .setHolder(holder0)
                    .build();
            var holder2 = GenericHolder.newBuilder()
                    .setHolderHolder(holder1)
                    .build();
            var holder3 = GenericHolder.newBuilder()
                    .setGeneric(holder2)
                    .build();

            var field = Field.parse("generic.holder_holder.holder.val");
            assertThat(field.valueIn(holder3))
                    .isEqualTo(value);
        }

        @Test
        @DisplayName("if the value is enum")
        void enumValue() {
            var holder = GenericHolder.newBuilder()
                    .setCount(Count.TWO)
                    .build();

            var field = Field.parse("count");
            assertThat(field.valueIn(holder))
                    .isEqualTo(Count.TWO);
        }

        @Test
        @DisplayName("throwing `ISE` if missed and getting directly")
        void directFailure() {
            assertIllegalState(() -> missingField.valueIn(message));
        }

        @Test
        @DisplayName("failing if the path reaches over a primitive value")
        void failOnMissingField() {
            var value = "primitive value";
            var holder = StringHolder.newBuilder()
                    .setVal(value)
                    .build();
            var wrongPath = parse("val.this_field_is_absent");
            assertIllegalState(() -> wrongPath.valueIn(holder));
        }
    }

    @Test
    @DisplayName("check that the field is present in the message type")
    void checkPresent() {
        var field = Field.parse("val");
        var message = StringHolder.getDescriptor();
        assertThat(field.presentIn(message)).isTrue();
    }

    @Test
    @DisplayName("check that the field is not present in the message type")
    void checkNotPresent() {
        var field = Field.parse("some_other_field");
        var message = StringHolder.getDescriptor();
        assertThat(field.presentIn(message)).isFalse();
    }

    @Nested
    @DisplayName("obtain the descriptor of the field")
    class GettingDescriptor {

        @Test
        @DisplayName("if present")
        void ifFound() {
            assertThat(Field.named("val")
                                   .findDescriptor(AnyHolder.getDescriptor()))
                  .isPresent();
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            assertThat(Field.named("value") // the real name is `val`.
                                   .findDescriptor(StringHolder.getDescriptor()))
                  .isEmpty();
        }
    }

    @Nested
    @DisplayName("obtain the type of the field in a message type")
    class GettingType {

        @Test
        @DisplayName("for messages")
        void messageType() {
            assertThat(Field.parse("holder_holder.holder")
                                   .findType(GenericHolder.class))
                  .hasValue(StringHolder.class);
        }

        @Test
        @DisplayName("for enums")
        void primitiveType() {
            assertThat(Field.named("count").findType(GenericHolder.class))
                  .hasValue(Count.class);
        }

        @Test
        @DisplayName("for primitives")
        void enumType() {
            assertThat(Field.named("size").findType(GenericHolder.class))
                  .hasValue(int.class);
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            assertThat(Field.parse("holder")
                                   .findType(GenericHolder.class))
                  .isEmpty();
        }

        @Test
        @DisplayName("when the type is recursive")
        void recursiveType() {
            var field = Field.parse("generic.generic.generic.generic.generic");

            assertThat(field.findType(GenericHolder.class))
                  .hasValue(GenericHolder.class);
        }
    }

    @Nested
    @DisplayName("obtain the name of the field by its number")
    class ByNumber {

        private final Descriptor message = Timestamp.getDescriptor();

        @Test
        @DisplayName("returning the short name of the field, if present")
        void nameValue() {
            var name = Field.nameOf(Timestamp.NANOS_FIELD_NUMBER, message);
            assertThat(name).isEqualTo("nanos");
        }

        @Nested
        @DisplayName("throwing")
        class Throwing {

            @Test
            @DisplayName("`IllegalArgumentException` for non-positive number")
            void zeroOrNegative() {
                assertIllegalArgument(() -> nameOf(-1, message));
                assertIllegalArgument(() -> nameOf(0, message));
            }

            @Test
            @DisplayName("`IllegalStateException` if there is no field with such number")
            void noField() {
                assertIllegalState(() -> nameOf(100, message));
            }
        }
    }

    @Test
    @DisplayName("obtain the instance which is a nested field in the current field type")
    void returnNested() {
        var topLevelField = "top-level-field";
        var topLevel = Field.named(topLevelField);
        var nestedField = "nested-field";
        var nested = topLevel.nested(nestedField);

        var path = nested.path();
        assertThat(path.getFieldNameList()).containsExactly(topLevelField, nestedField);
    }

    @Test
    @DisplayName("obtain the instance by number in a message type")
    void byNumber() {
        var seconds = Field.withNumberIn(1, Timestamp.getDescriptor());
        assertThat((Long) seconds.valueIn(Time.currentTime()))
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("check that the field is a nested field")
    void checkNested() {
        var field = Field.parse("some.nested.field");
        assertThat(field.isNested()).isTrue();
    }

    @Test
    @DisplayName("check that the field is not a nested field")
    void checkTopLevel() {
        var field = Field.parse("top_level_field");
        assertThat(field.isNested()).isFalse();
    }
}
