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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.fs.AbstractSourceFile;
import io.spine.type.MessageType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A Protobuf file that also gives access to its {@link FileDescriptor descriptor}.
 */
public class SourceFile extends AbstractSourceFile {

    private final FileDescriptor descriptor;

    protected SourceFile(FileDescriptor descriptor) {
        super(toPath(descriptor));
        this.descriptor = descriptor;
    }

    /**
     * Creates a new instance for the passed file descriptor.
     */
    public static SourceFile from(FileDescriptor file) {
        return new SourceFile(file);
    }

    private static Path toPath(FileDescriptor file) {
        checkNotNull(file);
        var result = Paths.get(file.getName());
        return result;
    }

    /**
     * Obtains the descriptor of the file.
     */
    public FileDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Obtains all top-level (i.e. non-nested) message types declared in this file set.
     */
    public List<MessageType> topLevelMessages() {
        List<MessageType> result =
                descriptor.getMessageTypes()
                          .stream()
                          .map(MessageType::new)
                          .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains all message declarations that match the passed predicate.
     */
    public List<MessageType> allThat(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageType> result = ImmutableList.builder();
        for (var messageType : descriptor.getMessageTypes()) {
            var declaration = new MessageType(messageType);
            if (predicate.test(messageType.toProto())) {
                result.add(declaration);
            }
            Collection<MessageType> allNested =
                    declaration.nestedTypesThat(predicate);
            result.addAll(allNested);
        }
        return result.build();
    }

}
