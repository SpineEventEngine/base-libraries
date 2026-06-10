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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.base.MessageFile;
import io.spine.code.fs.AbstractFileName;

import java.io.Serial;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A name of a Protobuf source code file.
 */
@Immutable
public class FileName extends AbstractFileName<FileName> implements UnderscoredName {

    @Serial
    private static final long serialVersionUID = 0L;

    /** The standard file extension. */
    public static final String EXTENSION = ".proto";

    /** The file system separator as defined by Protobuf. Not platform-dependent. */
    private static final char PATH_SEPARATOR = '/';

    private FileName(String value) {
        super(value);
    }

    /**
     * Creates a new proto file name with the given value.
     */
    public static FileName of(String value) {
        checkNotEmptyOrBlank(value);
        checkArgument(value.endsWith(EXTENSION));
        return new FileName(value);
    }

    /**
     * Obtains the file name from the given descriptor message.
     */
    public static FileName from(FileDescriptorProto descriptor) {
        checkNotNull(descriptor);
        var result = of(descriptor.getName());
        return result;
    }

    /**
     * Obtains the file name from the passed descriptor.
     */
    public static FileName from(FileDescriptor descriptor) {
        return from(descriptor.toProto());
    }

    /**
     * Obtains an immutable list of words used in the name of the file.
     */
    @Override
    public List<String> words() {
        var words = nameOnly().split(WORD_SEPARATOR);
        var result = ImmutableList.copyOf(words);
        return result;
    }

    /**
     * Obtains the file name without path and extension.
     */
    private String nameOnly() {
        var name = nameWithoutExtension();
        var lastBackslashIndex = name.lastIndexOf(PATH_SEPARATOR);
        var result = name.substring(lastBackslashIndex + 1);
        return result;
    }

    /**
     * Returns the file name with an extension but without a path.
     */
    public String nameWithExtension() {
        var fullName = value();
        var lastBackslashIndex = fullName.lastIndexOf(PATH_SEPARATOR);
        var result = fullName.substring(lastBackslashIndex + 1);
        return result;
    }

    /**
     * Returns the file name without path and extension in the {@code CamelCase}.
     */
    public String nameOnlyCamelCase() {
        return toCamelCase();
    }

    /**
     * Returns the file name without extension but including path.
     */
    public String nameWithoutExtension() {
        var value = value();
        var extensionIndex = value.lastIndexOf(EXTENSION);
        var result = value.substring(0, extensionIndex);
        return result;
    }

    private boolean matches(MessageFile file) {
        var result = value().endsWith(file.suffix());
        return result;
    }

    /**
     * Returns {@code true} if the name of the file matches convention for command message files.
     */
    public boolean isCommands() {
        return matches(MessageFile.COMMANDS);
    }

    /**
     * Returns {@code true} if the name of the file matches convention for event message files.
     */
    public boolean isEvents() {
        return matches(MessageFile.EVENTS);
    }

    /**
     * Returns {@code true} if the name of the file matches convention for rejection message files.
     */
    public boolean isRejections() {
        return matches(MessageFile.REJECTIONS);
    }
}
