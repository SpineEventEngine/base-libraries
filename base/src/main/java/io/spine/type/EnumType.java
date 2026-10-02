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
