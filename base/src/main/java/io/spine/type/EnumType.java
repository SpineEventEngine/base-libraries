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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.code.proto.TypeSet;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An enumeration type.
 */
@Internal
public final class EnumType extends Type<EnumDescriptor, EnumDescriptorProto> {

    private EnumType(EnumDescriptor descriptor) {
        super(descriptor, false);
    }

    @Override
    public EnumDescriptorProto toProto() {
        return descriptor().toProto();
    }

    @Override
    public TypeUrl url() {
        return TypeUrl.from(descriptor());
    }

    @Override
    public ClassName javaClassName() {
        return ClassName.from(descriptor());
    }

    @Override
    public Optional<Type<Descriptor, DescriptorProto>> containingType() {
        var parent = descriptor().getContainingType();
        return Optional.ofNullable(parent)
                       .map(MessageType::new);
    }

    public static EnumType create(EnumDescriptor descriptor) {
        return new EnumType(descriptor);
    }

    @SuppressWarnings("MethodWithMultipleLoops")
        // Need to go through top-level enums and those nested messages.
    public static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        var result = TypeSet.newBuilder();

        for (var enumDescriptor : file.getEnumTypes()) {
            result.add(create(enumDescriptor));
        }

        for (var messageType : file.getMessageTypes()) {
            addNested(messageType, result);
        }
        return result.build();
    }

    @SuppressWarnings("MethodWithMultipleLoops") // Need to go through enums and nested messages.
    private static void addNested(Descriptor messageType, TypeSet.Builder set) {
        for (var enumDescriptor : messageType.getEnumTypes()) {
            set.add(create(enumDescriptor));
        }

        for (var nestedType : messageType.getNestedTypes()) {
            addNested(nestedType, set);
        }
    }
}
