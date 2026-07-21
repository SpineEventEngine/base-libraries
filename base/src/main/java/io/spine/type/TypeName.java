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
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Message;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A fully qualified Protobuf type name.
 */
@Immutable
public final class TypeName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * The separator character for package names in a fully qualified proto type name.
     */
    public static final char PACKAGE_SEPARATOR = '.';

    /**
     * The splitter to separate package and type names in fully-qualified type names.
     */
    private static final Splitter packageSplitter = Splitter.on(PACKAGE_SEPARATOR);

    /**
     * The character to separate a nested type from the outer type name.
     */
    public static final char NESTED_TYPE_SEPARATOR = '.';

    private TypeName(String value) {
        super(value);
    }

    private static TypeName create(String value) {
        return new TypeName(value);
    }

    /**
     * Creates a new instance with the given type name.
     */
    public static TypeName of(String typeName) {
        checkNotNull(typeName);
        checkArgument(!typeName.isEmpty());
        return create(typeName);
    }

    /**
     * Obtains a type name from the given type URL.
     */
    public static TypeName from(TypeUrl typeUrl) {
        checkNotNull(typeUrl);
        return typeUrl.typeName();
    }

    /**
     * Obtains type name for the passed message.
     */
    public static TypeName of(Message message) {
        checkNotNull(message);
        return from(TypeUrl.of(message));
    }

    /**
     * Obtains type name for the passed message class.
     */
    public static TypeName of(Class<? extends Message> cls) {
        checkNotNull(cls);
        return from(TypeUrl.of(cls));
    }

    /**
     * Obtains type name for the message type by its descriptor.
     */
    public static TypeName from(Descriptor descriptor) {
        checkNotNull(descriptor);
        return of(descriptor.getFullName());
    }

    /**
     * Returns the unqualified name of the Protobuf type, for example: {@code StringValue}.
     */
    public String simpleName() {
        var typeName = value();
        var tokens = packageSplitter.splitToList(typeName);
        var result = tokens.get(tokens.size() - 1);
        return result;
    }

    /**
     * Creates URL instance corresponding to this type name.
     */
    public TypeUrl toUrl() {
        return type().url();
    }

    /**
     * Returns a message {@link Class} corresponding to the Protobuf type represented
     * by this type name.
     *
     * @return the message class
     * @throws UnknownTypeException wrapping {@link ClassNotFoundException} if
     *         there is no corresponding Java class
     */
    public Class<?> toJavaClass() throws UnknownTypeException {
        return type().javaClass();
    }

    /**
     * Returns a message {@link Class} corresponding to the Protobuf message type represented
     * by this type name.
     *
     * <p>This is a convenience method. Use it only when you are sure that the name
     * represents a {@code Message} and is not an enum.
     *
     * @param <T> the type of the message
     * @throws UnknownTypeException if the type is not found among known types
     */
    public <T extends Message> Class<T> toMessageClass() throws UnknownTypeException {
        var cls = toJavaClass();
        checkState(Message.class.isAssignableFrom(cls));
        @SuppressWarnings("unchecked")
        var result = (Class<T>) cls;
        return result;
    }

    /**
     * Returns an enum class corresponding to this type name.
     *
     * <p>This is a convenience method. Use it only when you are sure that the name
     * represents an enum not a {@code Message}.
     *
     * @param <T> the type of the enum
     * @throws UnknownTypeException if the type is not found among known types
     */
    public <T extends Enum<?>> Class<T> toEnumClass() throws UnknownTypeException {
        var cls = toJavaClass();
        checkState(Enum.class.isAssignableFrom(cls));
        @SuppressWarnings("unchecked")
        var result = (Class<T>) cls;
        return result;
    }

    /**
     * Obtains the descriptor for the type.
     */
    public GenericDescriptor genericDescriptor() {
        return type().descriptor();
    }

    /**
     * Obtains the message descriptor for the type or throws an exception if this type is name
     * and it represents an enum.
     */
    public Descriptor messageDescriptor() {
        var result = (Descriptor) genericDescriptor();
        return result;
    }

    /**
     * Verifies if the type belongs to the passed package.
     */
    boolean belongsTo(String packageName) {
        var typeName = value();
        var inPackage = typeName.startsWith(packageName)
                && typeName.charAt(packageName.length()) == PACKAGE_SEPARATOR;
        return inPackage;
    }

    Type<?, ?> type() {
        var result = KnownTypes.instance()
                               .find(this)
                               .orElseThrow(() -> new UnknownTypeException(value()));
        return result;
    }
}
