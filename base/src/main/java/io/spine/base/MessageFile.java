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

package io.spine.base;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.proto.FileName;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An enumeration of file naming conventions for pre-defined types of messages.
 */
public enum MessageFile implements Predicate<FileDescriptorProto> {

    /**
     * Commands are declared in a file whose name ends with {@code "commands.proto"}.
     */
    COMMANDS("commands"),

    /**
     * Events are declared in a file whose name ends with {@code "events.proto"}.
     */
    EVENTS("events"),

    /**
     * Rejections are declared in a file whose name ends with {@code "rejections.proto"}.
     */
    REJECTIONS("rejections");

    private final String suffix;

    MessageFile(String name) {
        this.suffix = checkNotNull(name) + FileName.EXTENSION;
    }

    /**
     * Checks if the name of the given file matches this suffix.
     */
    @Override
    public boolean test(FileDescriptorProto file) {
        var name = file.getName();
        return name.endsWith(suffix);
    }

    /**
     * Obtains a suffix required for this kind of files.
     */
    public String suffix() {
        return suffix;
    }
}
