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
 * <h2>Memoization of unpacked messages</h2>
 *
 * <p>An instance of {@code Any} remembers the message it was unpacked into, so that
 * unpacking the same instance again does not parse the packed bytes anew.
 * Please mind the consequences:
 * <ul>
 *     <li>Unpacking the same instance of {@code Any} more than once may return the same
 *         instance of the message. It is safe because messages are immutable. Please
 *         compare unpacked messages using {@code equals()}, and do not rely on getting
 *         either the same or a new instance.
 *     <li>An unpacked instance of {@code Any} holds a strong reference to the parsed
 *         message for as long as the instance itself is reachable. A long-lived {@code Any}
 *         thus keeps both the serialized and the parsed forms of the message in memory.
 * </ul>
 *
 * @implNote Unpacking is delegated to {@link Any#unpackSameTypeAs(Message)}, which
 *  remembers the parsed message in the instance of {@code Any}. The details below
 *  describe the Protobuf Java runtime this library is built with.
 *  <ul>
 *      <li>The remembered message is not a part of the value of {@code Any}. It affects
 *          neither equality nor serialization, and it is not passed to a builder.
 *          An equal instance of {@code Any}, e.g. the one obtained by parsing,
 *          is unpacked anew.
 *      <li>Threads unpacking the same instance of {@code Any} for the first time
 *          simultaneously may each parse the bytes and get distinct but equal messages.
 *      <li>{@code Any} remembers the message along with its Java class, and refuses
 *          to unpack into another class representing the same Protobuf type, such as
 *          {@link com.google.protobuf.DynamicMessage DynamicMessage}.
 *          {@link UnexpectedTypeException} is thrown in such a case.
 *  </ul>
 *
 *  <p>This class does not use {@link Any#unpack(Class)} because that method obtains
 *  the default instance of the message by calling {@code getDefaultInstance()}
 *  reflectively each time it parses the bytes. The exemplar passed to
 *  {@code unpackSameTypeAs()} comes from {@link Messages#getDefaultInstance(Class)},
 *  which performs such a call only once per message class.
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
     * <p>The given class must represent the Protobuf type named in the type URL of
     * the passed {@code Any}. The prefix of the type URL is not compared.
     *
     * <p>The passed instance of {@code Any} remembers the unpacked message. Unpacking
     * the same instance again may return the same message.
     * Please see the documentation of this class for the consequences.
     *
     * <p>Prefer this method over {@link Any#unpack(Class) Any.unpack(Class)}.
     * That method obtains the default instance of the message reflectively each time
     * it parses the packed bytes, while this method does so only once per message class.
     * This method also reports a failure with an unchecked exception.
     * Please see the "Implementation Note" section of this class for details.
     *
     * @param any
     *         instance of {@link Any} that should be unwrapped
     * @param cls
     *         the class implementing the type of the enclosed object
     * @param <T>
     *         the type enclosed into {@code Any}
     * @return unwrapped message instance
     * @throws UnexpectedTypeException
     *         if the type of the message packed into the passed {@code Any} differs from
     *         the type of the given class, if the {@code Any} remembers the message as
     *         an instance of another Java class, such as {@code DynamicMessage}, or if
     *         the packed bytes cannot be parsed into a message of the given class
     */
    public static <T extends Message> T unpack(Any any, Class<T> cls) {
        checkNotNull(any);
        checkNotNull(cls);

        var defaultInstance = Messages.getDefaultInstance(cls);
        try {
            var result = any.unpackSameTypeAs(defaultInstance);
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

    private static @Nullable Message unpackOrNull(@Nullable Any any) {
        return any == null
               ? null
               : unpack(any);
    }
}
