/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.base;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.proto.FileName;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A enumeration of file naming conventions for pre-defined types of messages.
 */
public enum MessageFile implements Predicate<FileDescriptorProto> {

    /**
     * Commands are declared in a file which name ends with {@code "commands.proto"}.
     */
    COMMANDS("commands"),

    /**
     * Events are declared in a file which name ends with {@code "events.proto"}.
     */
    EVENTS("events"),

    /**
     * Rejections are declared in a file which name ends with {@code "rejections.proto"}.
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
