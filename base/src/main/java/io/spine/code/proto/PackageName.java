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

package io.spine.code.proto;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.annotation.Internal;
import io.spine.value.StringTypeValue;

import java.io.Serial;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A name of a Protobuf package.
 */
@Immutable
@Internal
public final class PackageName extends StringTypeValue {

    @Serial
    private static final long serialVersionUID = 0L;
    private static final String DELIMITER = ".";
    private static final String OF_GOOGLE = "google";
    private static final PackageName GOOGLE_PROTOBUF = new PackageName("google.protobuf");

    private PackageName(String value) {
        super(value);
    }

    /**
     * Creates a new instance with the passed value.
     */
    public static PackageName of(String value) {
        checkNotNull(value);
        var result = new PackageName(value);
        return result;
    }

    /**
     * Obtains a package name for the passed message type.
     */
    public static PackageName of(Descriptor message) {
        checkNotNull(message);
        var result = of(message.getFile()
                               .getPackage());
        return result;
    }

    /**
     * Obtains the name of the Google Protobuf library package.
     */
    public static PackageName googleProtobuf() {
        return GOOGLE_PROTOBUF;
    }

    /**
     * Obtains Protobuf package delimiter.
     */
    public static String delimiter() {
        return DELIMITER;
    }

    /**
     * Verifies if the package represented by this package name is
     * <a href="https://developers.google.com/protocol-buffers/docs/proto3#packages-and-name-resolution">
     * nested</a> in the given package.
     */
    public boolean isInnerOf(PackageName parentCandidate) {
        checkNotNull(parentCandidate);
        var result = value().startsWith(parentCandidate.value());
        return result;
    }

    /**
     * Tells if this package belongs to Google code.
     */
    public boolean isGoogle() {
        return value().startsWith(OF_GOOGLE);
    }
}
