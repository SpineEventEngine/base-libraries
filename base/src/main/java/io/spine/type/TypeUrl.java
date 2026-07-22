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

import com.google.common.base.Splitter;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Any;
import com.google.protobuf.AnyOrBuilder;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.annotation.VisibleForTesting;
import io.spine.code.proto.PackageName;
import io.spine.option.OptionsProto;
import io.spine.protobuf.Messages;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;

/**
 * A URL of a Protobuf type.
 *
 * <p>Consists of the two parts separated with a slash.
 * The first part is the type URL prefix (for example, {@code "type.googleapis.com"}).
 * The second part is a {@linkplain Descriptor#getFullName()
 * fully-qualified Protobuf type name}.
 *
 * @see Any#getTypeUrl()
 */
@Immutable
public final class TypeUrl implements Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
    private static final String SEPARATOR = "/";
    private static final Splitter splitter = Splitter.on(SEPARATOR);

    /** The prefix of the type URL. */
    private final String prefix;

    /** The name of the Protobuf type. */
    private final TypeName typeName;

    private TypeUrl(String prefix, String typeName) {
        this.prefix = checkNotNull(prefix);
        this.typeName = TypeName.of(checkNotEmptyOrBlank(typeName));
    }

    /**
     * Creates a new {@code TypeUrl}.
     */
    private static TypeUrl create(String prefix, String typeName) {
        return new TypeUrl(prefix, typeName);
    }

    @VisibleForTesting
    static String composeTypeUrl(String prefix, String typeName) {
        var url = prefix + SEPARATOR + typeName;
        return url;
    }

    /**
     * Creates a new type URL taking it from the passed message instance.
     *
     * @param msg an instance to get the type URL from
     */
    public static TypeUrl of(Message msg) {
        checkNotNull(msg);
        return from(msg.getDescriptorForType());
    }

    /**
     * Creates a new instance by the passed message descriptor taking its type URL.
     *
     * @param descriptor the descriptor of the type
     */
    public static TypeUrl from(Descriptor descriptor) {
        checkNotNull(descriptor);
        return ofTypeOrService(descriptor);
    }

    /**
     * Creates a new instance by the passed enum descriptor taking its type URL.
     *
     * @param descriptor the descriptor of the type
     */
    public static TypeUrl from(EnumDescriptor descriptor) {
        checkNotNull(descriptor);
        return ofTypeOrService(descriptor);
    }

    /**
     * Creates a new instance by the passed service descriptor taking its type URL.
     *
     * @param descriptor the descriptor of the type
     */
    public static TypeUrl from(ServiceDescriptor descriptor) {
        checkNotNull(descriptor);
        return ofTypeOrService(descriptor);
    }

    /**
     * Obtains a type URL of a descriptor representing a message type, enum type, or service.
     *
     * @param descriptor
     *         the descriptor for obtaining the type URL
     * @return the type URL
     * @throws IllegalArgumentException if the given descriptor is not of one of these
     *         types: {@link Descriptor}, {@link EnumDescriptor},{@link ServiceDescriptor}
     */
    public static TypeUrl ofTypeOrService(GenericDescriptor descriptor) {
        checkNotNull(descriptor);
        if(!(descriptor instanceof Descriptor ||
             descriptor instanceof EnumDescriptor ||
             descriptor instanceof ServiceDescriptor)) {
            throw newIllegalArgumentException(
                    "Only messages, enums, or services can have type URLs." +
                            " Encountered: `%s`.", descriptor.getClass().getName()
            );
        }
        var prefix = prefixFor(descriptor);
        return create(prefix, descriptor.getFullName());
    }

    /**
     * Creates a new instance from the passed type URL.
     *
     * @param typeUrl the type URL of a Protobuf declaration such as message, enum, or service
     */
    @Internal
    public static TypeUrl parse(String typeUrl) {
        checkNotNull(typeUrl);
        checkArgument(!typeUrl.isEmpty());
        checkTypeUrlFormat(typeUrl);
        var result = doParse(typeUrl);
        return result;
    }

    private static void checkTypeUrlFormat(String str) {
        if (!str.contains(SEPARATOR)) {
            throw newIllegalArgumentException("Malformed type URL: `%s`.", str);
        }
    }

    private static TypeUrl doParse(String typeUrl) {
        var strings = splitter.splitToList(typeUrl);
        if (strings.size() != 2) {
            throw malformedTypeUrl(typeUrl);
        }
        var prefix = strings.get(0);
        var typeName = strings.get(1);
        return create(prefix, typeName);
    }

    private static IllegalArgumentException malformedTypeUrl(String typeUrl) {
        var errMsg = format("Invalid Protobuf type URL encountered: `%s`.", typeUrl);
        throw new IllegalArgumentException(new InvalidProtocolBufferException(errMsg));
    }

    /**
     * Obtains the type URL of the message enclosed into the instance of {@link Any}.
     *
     * @param any the instance of {@code Any} containing a {@code Message} instance of interest
     * @return a type URL
     */
    public static TypeUrl ofEnclosed(AnyOrBuilder any) {
        var typeUrl = doParse(any.getTypeUrl());
        return typeUrl;
    }

    /**
     * Obtains the type URL for the passed message class.
     */
    public static TypeUrl of(Class<? extends Message> cls) {
        var defaultInstance = Messages.getDefaultInstance(cls);
        var result = of(defaultInstance);
        return result;
    }

    /**
     * Obtains the prefix for the passed proto type.
     *
     * <p>If the type is a standard proto type, the {@linkplain Prefix#GOOGLE_APIS standard prefix}
     * is returned.
     *
     * <p>For custom types, returns the value specified in the {@linkplain
     * OptionsProto#typeUrlPrefix file option}.
     */
    private static String prefixFor(GenericDescriptor descriptor) {
        var file = descriptor.getFile();
        if (file.getPackage()
                .startsWith(PackageName.googleProtobuf()
                                       .value())) {
            return Prefix.GOOGLE_APIS.value();
        }
        var result = file.getOptions()
                         .getExtension(OptionsProto.typeUrlPrefix);
        return result;
    }

    /**
     * Returns a message {@link Class} corresponding to the Protobuf type represented
     * by this type URL.
     *
     * @return the Java class representing the Protobuf type
     * @throws UnknownTypeException if there is no corresponding Java class
     */
    public Class<?> toJavaClass() throws UnknownTypeException {
        return type().javaClass();
    }

    /**
     * Returns a message {@link Class} corresponding to the Protobuf message type represented
     * by this type URL.
     *
     * <p>This is a convenience method. Use it only when you are sure the {@code TypeUrl} represents
     * a message (i.e., not an enum).
     *
     * @throws IllegalStateException if the type URL represents an enum
     */
    public <T extends Message> Class<T> getMessageClass() throws UnknownTypeException {
        return typeName().toMessageClass();
    }

    /**
     * Obtains the prefix of the type URL.
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Obtains the value of the type URL.
     */
    @Override
    public String toString() {
        return value();
    }

    /**
     * Obtains the type name component of this type URL.
     */
    public TypeName typeName() {
        return typeName;
    }

    /**
     * Obtains the string representation of the URL.
     */
    public String value() {
        var result = composeTypeUrl(prefix, typeName.value());
        return result;
    }

    private Type<?, ?> type() throws UnknownTypeException {
        return typeName().type();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeUrl typeUrl)) {
            return false;
        }
        return Objects.equals(prefix, typeUrl.prefix) &&
               Objects.equals(typeName, typeUrl.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, typeName);
    }

    /**
     * Enumeration of known type URL prefixes.
     */
    public enum Prefix {

        /**
         * The prefix for standard Protobuf types.
         */
        @SuppressWarnings("DuplicateStringLiteralInspection") // the other is in Protobuf itself.
        GOOGLE_APIS("type.googleapis.com"),

        /**
         * The prefix for types provided by the Spine framework.
         */
        SPINE("type.spine.io");

        private final String value;

        Prefix(String value) {
            this.value = value;
        }

        /**
         * Obtains the value of the prefix.
         */
        public String value() {
            return value;
        }

        /**
         * Returns the value of the prefix.
         */
        @Override
        public String toString() {
            return value();
        }
    }
}
