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

package io.spine.code.java;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.value.StringTypeValue;

import java.io.Serial;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A Java package name.
 */
@Immutable
public final class PackageName extends StringTypeValue {

    @Serial
    private static final long serialVersionUID = 0L;
    private static final char DELIMITER_CHAR = '.';
    private static final String DELIMITER = String.valueOf(DELIMITER_CHAR);

    private PackageName(String value) {
        super(checkNotEmptyOrBlank(value));
    }

    /**
     * Creates an instance for the passed package name.
     *
     * @param value
     *         the package name, which cannot be empty or blank
     * @return new instance
     */
    public static PackageName of(String value) {
        checkNotNull(value);
        var result = new PackageName(value);
        return result;
    }

    /**
     * Creates an instance for the passed class.
     *
     * @param cls
     *         the class to create the package for
     * @return a new instance
     */
    public static PackageName of(Class<?> cls) {
        checkNotNull(cls);
        return of(cls.getPackage()
                     .getName());
    }

    /**
     * Creates a package nested into this one.
     *
     * @param name a short name of the nested package
     * @return nested package
     */
    public PackageName nested(String name) {
        checkNotNull(name);
        var result = of(value() + delimiter() + name);
        return result;
    }

    /**
     * Obtains Java package delimiter as a {@code String}.
     */
    public static String delimiter() {
        return DELIMITER;
    }

    /**
     * Obtains Java package delimiter as a single {@code char}.
     */
    public static char delimiterChar() {
        return DELIMITER_CHAR;
    }

    /**
     * Obtains a Java package name by the passed file descriptor.
     */
    public static PackageName resolve(FileDescriptorProto file) {
        var javaPackage = resolveName(file).trim();
        checkArgument(
                !javaPackage.isEmpty(),
                "Message classes generated from the file `%s` belong to the default package.%n"
                        + "Please use `option java_package` or `package`"
                        + " to specify a Java package for these types.", file.getName()
        );
        var result = new PackageName(javaPackage);
        return result;
    }

    private static String resolveName(FileDescriptorProto file) {
        var javaPackage = file.getOptions()
                              .getJavaPackage();
        if (isNullOrEmpty(javaPackage)) {
            javaPackage = file.getPackage();
        }
        return javaPackage;
    }
}
