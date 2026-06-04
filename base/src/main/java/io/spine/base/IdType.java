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

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.protobuf.AnyPacker;
import io.spine.protobuf.Messages;
import io.spine.protobuf.TypeConverter;

/**
 * Supported types of identifiers.
 */
@SuppressWarnings({
        "OverlyStrongTypeCast" /* We cast to message types instead of `OrBuilder` types for
            the sake of clarity since `fromMessage()` methods accept only `Message` parameters. */,
        "unchecked" /* We ensure type by matching it first. */
})
enum IdType {

    STRING {
        @Override
        <I> boolean matchValue(I id) {
            return id instanceof String;
        }

        @Override
        boolean matchMessage(Message message) {
            return message instanceof StringValue;
        }

        @Override
        <I> boolean matchClass(Class<I> idClass) {
            return String.class.equals(idClass);
        }

        @Override
        String fromMessage(Message message) {
            return ((StringValue) message).getValue();
        }

        @Override
        <I> I defaultValue(Class<I> idClass) {
            return (I) "";
        }

        @Override
        boolean matchField(FieldDescriptor field) {
            return FieldDescriptor.JavaType.STRING == field.getJavaType();
        }
    },

    INTEGER {
        @Override
        <I> boolean matchValue(I id) {
            return id instanceof Integer;
        }

        @Override
        boolean matchMessage(Message message) {
            return message instanceof Int32Value;
        }

        @Override
        <I> boolean matchClass(Class<I> idClass) {
            return Integer.class.equals(idClass);
        }

        @Override
        Integer fromMessage(Message message) {
            return ((Int32Value) message).getValue();
        }

        @Override
        <I> I defaultValue(Class<I> idClass) {
            return (I) Integer.valueOf(0);
        }

        @Override
        boolean matchField(FieldDescriptor field) {
            return FieldDescriptor.JavaType.INT == field.getJavaType();
        }
    },

    LONG {
        @Override
        <I> boolean matchValue(I id) {
            return id instanceof Long;
        }

        @Override
        boolean matchMessage(Message message) {
            return message instanceof Int64Value;
        }

        @Override
        <I> boolean matchClass(Class<I> idClass) {
            return Long.class.equals(idClass);
        }

        @Override
        Long fromMessage(Message message) {
            return ((Int64Value) message).getValue();
        }

        @Override
        <I> I defaultValue(Class<I> idClass) {
            return (I) Long.valueOf(0);
        }

        @Override
        boolean matchField(FieldDescriptor field) {
            return FieldDescriptor.JavaType.LONG == field.getJavaType();
        }
    },

    MESSAGE {
        @Override
        <I> boolean matchValue(I id) {
            return id instanceof Message;
        }

        /**
         * Verifies if the passed message is not an instance of a wrapper for
         * simple types that are used for packing simple Java types into {@code Any}.
         *
         * @return {@code true} if the message is neither {@code StringValue}, nor
         *         {@code Int32Value}, nor {@code Int64Value}
         * @see Identifier#unpack(Any)
         */
        @Override
        boolean matchMessage(Message message) {
            return !(message instanceof StringValue
                    || message instanceof Int32Value
                    || message instanceof Int64Value);
        }

        @Override
        <I> boolean matchClass(Class<I> idClass) {
            return Message.class.isAssignableFrom(idClass);
        }

        @Override
        <I> Message toMessage(I id) {
            return (Message) id;
        }

        @Override
        Message fromMessage(Message message) {
            return message;
        }

        @Override
        <I> I defaultValue(Class<I> idClass) {
            var msgClass = (Class<? extends Message>) idClass;
            var result = Messages.getDefaultInstance(msgClass);
            return (I) result;
        }

        /**
         * Returns {@code true} if the given field is a message.
         *
         * <p>It does not necessarily mean that the type of identifiers matches.
         * Obtaining the class of the field is needed in this case.
         */
        @Override
        boolean matchField(FieldDescriptor field) {
            return FieldDescriptor.JavaType.MESSAGE == field.getJavaType();
        }
    };

    /**
     * Obtains the type for the passed ID value.
     *
     * @throws IllegalArgumentException
     *         if the passed value is not of supported type
     */
    static <I> IdType of(I id) {
        for (var type : values()) {
            if (type.matchValue(id)) {
                return type;
            }
        }
        throw Identifier.unsupported(id);
    }

    /**
     * Returns {@code true} if the passed instances of {@link Object} matches this
     * type of identifiers; {@code false} otherwise.
     */
    abstract <I> boolean matchValue(I id);

    /**
     * Returns {@code true} if the passed instance of {@code Message} matches
     * the type of Protobuf implementation of this type of identifier; {@code false} otherwise.
     */
    abstract boolean matchMessage(Message message);

    /**
     * Returns {@code true} if the passed class matches the one supported by this
     * type of identifiers; {@code false} otherwise.
     */
    abstract <I> boolean matchClass(Class<I> idClass);

    /**
     * Verifies if the passed field definition matches this type of identifier.
     */
    abstract boolean matchField(FieldDescriptor field);

    /**
     * Converts the passed Protobuf implementation instance of an identifier into
     * an {@code Object} of the corresponding type.
     */
    abstract Object fromMessage(Message message);

    /**
     * Obtains the default ID value for this type of identifier.
     */
    abstract <I> I defaultValue(Class<I> idClass);

    /**
     * Converts the passed ID object into the Protobuf implementation instance.
     */
    <I> Message toMessage(I id) {
        var message = TypeConverter.toMessage(id);
        return message;
    }

    /**
     * Converts the passed instance of the identifier into Protobuf implementation, and
     * then packs it into {@code Any}.
     */
    <I> Any pack(I id) {
        var msg = toMessage(id);
        var result = AnyPacker.pack(msg);
        return result;
    }
}
