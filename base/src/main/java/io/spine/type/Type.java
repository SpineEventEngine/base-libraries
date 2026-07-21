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

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FileName;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Protobuf type, such as a message or an enum.
 *
 * @param <T>
 *         the type of the type descriptor
 * @param <P>
 *         the type of the proto message of the descriptor
 */
@Immutable(containerOf = {"T", "P"})
@Internal
public abstract class Type<T extends GenericDescriptor, P extends Message> {

    /**
     * A cache of known classes per {@linkplain #javaClassName() Java class name}.
     *
     * <p>Saves the memory by limiting the maximum number of cached objects.
     *
     * @implNote {@link com.google.common.cache.LoadingCache LoadingCache} implementation
     *         would make dealing with the exceptions a bit more difficult to maintain and read.
     *         Which is why a straightforward get-put approach with a plain {@code Cache}
     *         implementation is used.
     */
    private static final Cache<String, Class<?>> knownClasses =
            CacheBuilder.newBuilder()
                        .maximumSize(1_000)
                        .build();

    private final T descriptor;
    private final boolean supportsBuilders;

    protected Type(T descriptor, boolean supportsBuilders) {
        this.descriptor = checkNotNull(descriptor);
        this.supportsBuilders = supportsBuilders;
    }

    /**
     * Obtains the descriptor of the type.
     */
    public T descriptor() {
        return this.descriptor;
    }

    /**
     * Obtains a file in which the type is declared.
     */
    public FileDescriptor file() {
        return this.descriptor.getFile();
    }

    /**
     * Obtains the proto message of the type descriptor.
     */
    public abstract P toProto();

    /**
     * Obtains the {@linkplain TypeName name} of this type.
     */
    public TypeName name() {
        return url().typeName();
    }

    /**
     * Obtains the {@link TypeUrl} of this type.
     */
    public abstract TypeUrl url();

    /**
     * Loads the Java class representing this Protobuf type.
     */
    public Class<?> javaClass() {
        var clsName = javaClassName().value();
        var result = knownClasses.getIfPresent(clsName);
        if (result == null) {
            try {
                result = Class.forName(clsName);
                knownClasses.put(clsName, result);
            } catch (ClassNotFoundException e) {
                throw new UnknownTypeException(descriptor.getFullName(), e);
            }
        }
        return result;
    }

    /**
     * Obtains the name of the Java class representing this Protobuf type.
     */
    public abstract ClassName javaClassName();

    /**
     * Obtains the package for the corresponding Java type.
     */
    public PackageName javaPackage() {
        var file = descriptor.getFile().toProto();
        var result = PackageName.resolve(file);
        return result;
    }

    /**
     * Obtains the simple class name for the corresponding Java type.
     */
    public final SimpleClassName simpleJavaClassName() {
        return javaClassName().toSimple();
    }

    /**
     * Obtains the name of this type including the names of all the containing types but excluding
     * the package.
     */
    public final NestedTypeName nestedSimpleName() {
        return NestedTypeName.of(this);
    }

    /**
     * Obtains the type that contains the declaration of this type.
     *
     * @return the containing type or {@code Optional.empty()} if this type is top level
     */
    public abstract Optional<Type<Descriptor, DescriptorProto>> containingType();

    /**
     * Defines whether the Java class generated from this type has a builder.
     *
     * @return {@code true} if the generated Java class has corresponding
     *         {@link com.google.protobuf.Message.Builder} and
     *         {@link com.google.protobuf.MessageOrBuilder}
     */
    public final boolean supportsBuilders() {
        return supportsBuilders;
    }

    /**
     * Obtains {@code FileName} of a declaring Protobuf file.
     */
    public FileName declaringFileName() {
        return FileName.from(file());
    }

    /**
     * Returns a fully-qualified name of the Protobuf type.
     */
    @Override
    public String toString() {
        return descriptor.getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Type)) {
            return false;
        }
        var type = (Type<?, ?>) o;
        return Objects.equal(descriptor.getFullName(), type.descriptor.getFullName());
    }

    @Override
    public int hashCode() {
        return descriptor.getFullName()
                         .hashCode();
    }
}
