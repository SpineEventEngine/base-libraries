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
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.signature.qual.ClassGetSimpleName;

import java.io.Serial;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A {@link Class#getSimpleName() simple name} of a class.
 */
@Immutable
public final class SimpleClassName extends StringTypeValue {

    static final String OR_BUILDER_SUFFIX = "OrBuilder";

    @Serial
    private static final long serialVersionUID = 0L;
    private static final SimpleClassName BUILDER_CLASS_NAME = new SimpleClassName("Builder");

    private SimpleClassName(String value) {
        super(value);
    }

    /**
     * Creates a new instance.
     *
     * @param value cannot be null or empty, no other checking is performed
     * @return new instance
     */
    public static SimpleClassName create(@ClassGetSimpleName String value) {
        checkNotNull(value);
        checkArgument(!value.isEmpty(), "Simple class name cannot be empty.");
        return new SimpleClassName(value);
    }

    /**
     * Obtains the simple name of the given class.
     */
    public static SimpleClassName of(Class<?> cls) {
        checkNotNull(cls);
        var result = create(cls.getSimpleName());
        return result;
    }

    /**
     * Creates an instance with the outer class name for the types declared in the file specified
     * by the passed descriptor.
     *
     * <p>The outer class name is calculated according to
     * <a href="https://developers.google.com/protocol-buffers/docs/reference/java-generated#invocation">
     * Protobuf compiler conventions</a>.
     *
     * @param file a descriptor for file for which outer class name will be generated
     * @return outer class name
     */
    public static SimpleClassName outerOf(FileDescriptorProto file) {
        checkNotNull(file);
        var value = outerClassNameOf(file);
        var result = create(value);
        return result;
    }

    /**
     * Creates an instance with the outer class name for the types declared in the file specified
     * by the passed descriptor.
     */
    public static SimpleClassName outerOf(FileDescriptor file) {
        return outerOf(file.toProto());
    }

    /**
     * Obtains an outer class name declared in the passed file.
     *
     * @param  file the descriptor of the proto file
     * @return the value declared in the file options or
     *         {@linkplain Optional#empty() empty Optional} if the option is not set
     */
    public static Optional<SimpleClassName> declaredOuterClassName(FileDescriptor file) {
        var className = declaredOuterClassName(file.toProto());
        if (className.isEmpty()) {
            return Optional.empty();
        }
        var result = outerOf(file);
        return Optional.of(result);
    }

    private static String declaredOuterClassName(FileDescriptorProto file) {
        var result = file.getOptions().getJavaOuterClassname();
        return result;
    }

    /**
     * Calculates a name of an outer Java class for types declared in the file represented
     * by the passed descriptor.
     *
     * <p>The outer class name is calculated according to
     * <a href="https://developers.google.com/protocol-buffers/docs/reference/java-generated#invocation">
     * Protobuf compiler conventions</a>.
     *
     * @param file
     *         a descriptor for file for which outer class name will be generated
     * @return non-qualified outer class name
     */
    private static String outerClassNameOf(FileDescriptorProto file) {
        checkNotNull(file);
        var nameDeclaredInOptions = declaredOuterClassName(file);
        if (!nameDeclaredInOptions.isEmpty()) {
            return nameDeclaredInOptions;
        }
        var className = io.spine.code.proto.FileName.from(file)
                                                    .nameOnlyCamelCase();
        return className;
    }

    /**
     * Obtains default name for a builder class.
     */
    public static SimpleClassName ofBuilder() {
        return BUILDER_CLASS_NAME;
    }

    /**
     * Obtains class name for {@link com.google.protobuf.MessageOrBuilder MessageOrBuilder}
     * descendant for the passed message type.
     */
    public static SimpleClassName messageOrBuilder(@ClassGetSimpleName String typeName) {
        checkNotEmptyOrBlank(typeName);
        var result = create(typeName + OR_BUILDER_SUFFIX);
        return result;
    }

    /**
     * Obtains a Java class name corresponding the proto message declaration.
     */
    public static SimpleClassName ofMessage(Descriptor descriptor) {
        checkNotNull(descriptor);
        var result = create(descriptor.getName());
        return result;
    }

    /**
     * Creates a new instance with appended suffix.
     */
    public SimpleClassName with(String suffix) {
        checkNotEmptyOrBlank(suffix);
        return create(value() + suffix);
    }
}
