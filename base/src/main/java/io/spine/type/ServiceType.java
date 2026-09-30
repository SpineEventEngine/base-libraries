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
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.code.proto.TypeSet;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Protobuf service type as declared in a proto file.
 */
@Internal
public final class ServiceType extends Type<ServiceDescriptor, ServiceDescriptorProto> {

    private ServiceType(ServiceDescriptor descriptor) {
        super(descriptor, false);
    }

    /**
     * Creates a new instance from the given service descriptor.
     *
     * @param descriptor
     *         the service descriptor
     * @return new instance of {@code ServiceType}
     */
    public static ServiceType of(ServiceDescriptor descriptor) {
        checkNotNull(descriptor);
        return new ServiceType(descriptor);
    }

    /**
     * Collects all service types declared in the given file.
     */
    public static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        var result = TypeSet.newBuilder();
        for (var type : file.getServices()) {
            result.add(of(type));
        }
        return result.build();
    }

    @Override
    public ServiceDescriptorProto toProto() {
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
        // Services are not allowed to be nested in Protobuf.
        return Optional.empty();
    }
}
