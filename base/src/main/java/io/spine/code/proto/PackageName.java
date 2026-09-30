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
     * Obtains the Protobuf package delimiter.
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
