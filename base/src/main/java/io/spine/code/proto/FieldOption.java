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

package io.spine.code.proto;

import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

/**
 * A Protobuf option that is applied to fields in Protobuf messages.
 *
 * @param <F>
 *         value of this option
 */
@Immutable
public class FieldOption<@ImmutableTypeParameter F>
        extends AbstractOption<F, FieldDescriptor, FieldOptions> {

    /**
     * Creates an instance with the
     * <a href="https://developers.google.com/protocol-buffers/docs/proto3#custom_options">Protobuf
     * extension</a>
     * that corresponds to this option.
     */
    protected FieldOption(GeneratedExtension<FieldOptions, F> extension) {
        super(extension);
    }

    @Override
    protected FieldOptions optionsFrom(FieldDescriptor object) {
        return object.getOptions();
    }
}
