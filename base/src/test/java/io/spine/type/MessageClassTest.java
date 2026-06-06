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

package io.spine.type;

import com.google.common.testing.EqualsTester;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.StringValue;
import com.google.protobuf.UnknownFieldSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;

@DisplayName("MessageClass should")
class MessageClassTest {

    private static final Class<StringValue> MSG_CLASS = StringValue.class;
    private static final TypeUrl MSG_TYPE = TypeUrl.of(StringValue.class);
    private static final TypeUrl OTHER_TYPE = TypeUrl.of(Any.class);

    @SuppressWarnings("SerializableInnerClassWithNonSerializableOuterClass")
    @Test
    @DisplayName("provide equality within the class")
    void beEqualWithingClass() {
        new EqualsTester()
                .addEqualityGroup(new TestMessageClass(MSG_CLASS),
                                  new TestMessageClass(MSG_CLASS),
                                  new TestMessageClass(MSG_CLASS, MSG_TYPE))
                .addEqualityGroup(new TestMessageClass(MSG_CLASS, OTHER_TYPE),
                                  new TestMessageClass(MSG_CLASS, OTHER_TYPE))
                .testEquals();
    }

    @Test
    void serialize() {
        reserializeAndAssert(new TestMessageClass(MSG_CLASS));
    }

    @Test
    @DisplayName("obtain the name of the type")
    void typeName() {
        assertThat(new TestMessageClass(MSG_CLASS).typeName())
                .isEqualTo(MSG_TYPE.typeName());
    }

    /**
     * Cursory test obtaining interfaces extending {@code Message}.
     *
     * @implNote Since we don't have code generation capabilities that would generate interfaces
     * extending {@code Message} in this module, this test is limited, and simply makes use
     * of the method.
     */
    @Test
    @DisplayName("obtain super interfaces of a class")
    void interfaces() {
        assertThat(MessageClass.interfacesOf(StubMessage.class))
                .containsExactly(SubSubMessage.class, SubMessage.class, SuperMessage.class);
    }

    @Test
    @DisplayName("obtain the type URL")
    void typeUrl() {
        assertThat(new TestMessageClass(MSG_CLASS, MSG_TYPE).typeUrl())
                .isEqualTo(MSG_TYPE);
    }

    @Test
    @DisplayName("not be equal to an instance of a different type")
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    void notEqualToOtherType() {
        assertThat(new TestMessageClass(MSG_CLASS).equals("not a message class"))
                .isFalse();
    }

    @Test
    @DisplayName("not be equal to a message class wrapping another Java class")
    void notEqualToDifferentClassValue() {
        assertThat(new TestMessageClass(StringValue.class))
                .isNotEqualTo(new TestMessageClass(Any.class));
    }

    /**
     * Test environment class extending the abstract base.
     */
    private static class TestMessageClass extends MessageClass<Message> {

        private static final long serialVersionUID = 0L;

        private TestMessageClass(Class<? extends Message> value) {
            super(value);
        }

        private TestMessageClass(Class<? extends Message> value, TypeUrl url) {
            super(value, url);
        }
    }

    /**
     * Base test environment interface for {@link MessageClassTest#interfaces()}.
     */
    private interface SuperMessage extends Message {
    }

    private interface SubMessage extends SuperMessage {
    }

    private interface SubSubMessage extends SubMessage {
    }

    /**
     * Stub implementation for {@link MessageClassTest#interfaces()}.
     */
    @SuppressWarnings("ReturnOfNull")
    private static final class StubMessage implements SubSubMessage {

        @Override
        public void writeTo(CodedOutputStream output) {
        }

        @Override
        public int getSerializedSize() {
            return 0;
        }

        @Override
        public Parser<? extends Message> getParserForType() {
            return null;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }

        @SuppressWarnings("ZeroLengthArrayAllocation")
        @Override
        public byte[] toByteArray() {
            return new byte[0];
        }

        @Override
        public void writeTo(OutputStream output) {
        }

        @Override
        public void writeDelimitedTo(OutputStream output) {
        }

        @Override
        public Builder newBuilderForType() {
            return null;
        }

        @Override
        public Builder toBuilder() {
            return null;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return null;
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        public List<String> findInitializationErrors() {
            return null;
        }

        @Override
        public String getInitializationErrorString() {
            return null;
        }

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return null;
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            return null;
        }

        @Override
        public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
            return false;
        }

        @Override
        public Descriptors.FieldDescriptor getOneofFieldDescriptor(
                Descriptors.OneofDescriptor oneof) {
            return null;
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            return false;
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            return null;
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            return 0;
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            return null;
        }

        @Override
        public UnknownFieldSet getUnknownFields() {
            return null;
        }
    }
}
