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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.spine.type.TypeUrl;
import io.spine.type.UnexpectedTypeException;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for packing messages into {@link Any} and unpacking them.
 *
 * <p>When packing, the {@code AnyPacker} takes care of obtaining the correct type URL prefix
 * for the passed messages.
 *
 * <p>When unpacking, the {@code AnyPacker} obtains a Java class matching the type URL
 * from the given instance of {@link Any}.
 *
 * @implNote This class does not use the {@link Any#unpack(Class)} method for unpacking
 *  for performance reasons.
 *
 *  <p>The implementation of {@link Any#unpack(Class)} invokes the {@link Any#is(Class) is(Class)}
 *  method that obtains a default instance of a message by calling a method
 *  {@code getDefaultInstance()} reflectively.
 *
 *  <p>We are aiming for better performance by caching the default instances
 *  when {@link Messages#getDefaultInstance(Class)} is called.
 *
 * @see Any#pack(Message, String)
 * @see #unpack(Any)
 */
public final class AnyPacker {

    /**
     * Prevents the utility class instantiation.
     */
    private AnyPacker() {
    }

    /**
     * Wraps {@link Message} object inside of {@link Any} instance.
     *
     * <p>If an instance of {@code Any} is passed, this instance is returned.
     *
     * @param message the message to pack
     * @return the wrapping instance of {@link Any} or the message itself, if it is {@code Any}
     */
    public static Any pack(Message message) {
        checkNotNull(message);
        if (message instanceof Any any) {
            return any;
        }
        var typeUrl = TypeUrl.from(message.getDescriptorForType());
        var typeUrlPrefix = typeUrl.prefix();
        var result = Any.pack(message, typeUrlPrefix);
        return result;
    }

    /**
     * Unwraps {@code Any} value into an instance of the type specified by value
     * returned by {@link Any#getTypeUrl()}.
     *
     * @param any instance of {@link Any} that should be unwrapped
     * @return unwrapped message instance
     */
    public static Message unpack(Any any) {
        checkNotNull(any);
        var typeUrl = TypeUrl.ofEnclosed(any);
        Class<? extends Message> messageClass = typeUrl.getMessageClass();
        return unpack(any, messageClass);
    }

    /**
     * Unwraps {@link Any} value into an instance of the given class.
     *
     * <p>If there is no Java class for the type, {@link UnexpectedTypeException
     * UnexpectedTypeException} is thrown.
     *
     * <p>Prefer this function for unpacking over the {@link Any#unpack(Class)}
     * method for performance reasons.
     * Please see the "Implementation Note" section of this class for details.
     *
     * @param any
     *         instance of {@link Any} that should be unwrapped
     * @param cls
     *         the class implementing the type of the enclosed object
     * @param <T>
     *         the type enclosed into {@code Any}
     * @return unwrapped message instance
     */
    public static <T extends Message> T unpack(Any any, Class<T> cls) {
        checkNotNull(any);
        checkNotNull(cls);

        var defaultInstance = Messages.getDefaultInstance(cls);
        var expectedTypeUrl = TypeUrl.of(defaultInstance);
        checkType(any, expectedTypeUrl);
        try {
            @SuppressWarnings("unchecked")  // Ensured by the check above.
            var result = (T) defaultInstance.getParserForType()
                                            .parseFrom(any.getValue());
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw new UnexpectedTypeException(e);
        }
    }

    /**
     * Creates an iterator that packs each incoming message into {@code Any}.
     *
     * @param iterator the iterator over messages to pack
     * @return the packing iterator
     */
    public static Iterator<Any> pack(Iterator<Message> iterator) {
        checkNotNull(iterator);
        return new PackingIterator(iterator);
    }

    /**
     * Provides the function for unpacking messages from {@code Any}.
     *
     * <p>The function returns {@code null} for {@code null} input.
     */
    public static Function<@Nullable Any, @Nullable Message> unpackFunc() {
        return AnyPacker::unpackOrNull;
    }

    /**
     * Provides the function for unpacking messages of a given type from {@code Any}.
     *
     * <p>The function returns {@code null} for {@code null} input.
     *
     * <p>The function throws a {@link UnexpectedTypeException} if the actual type of the message
     * does not match the given class.
     *
     * @param type
     *         expected class of the messages
     */
    public static <T extends Message> Function<@Nullable Any, @Nullable T>
    unpackFunc(Class<T> type) {
        checkNotNull(type);
        return any -> any == null
                      ? null
                      : unpack(any, type);
    }

    private static void checkType(Any any, TypeUrl expectedType) {
        var actualType = TypeUrl.ofEnclosed(any);
        if (!actualType.equals(expectedType)) {
            throw new UnexpectedTypeException(expectedType, actualType);
        }
    }

    private static @Nullable Message unpackOrNull(@Nullable Any any) {
        return any == null
               ? null
               : unpack(any);
    }
}
