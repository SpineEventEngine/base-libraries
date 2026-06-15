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

package io.spine.code.java;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.annotation.Internal;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;
import org.jspecify.annotations.Nullable;

import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.code.java.SimpleClassName.OR_BUILDER_SUFFIX;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A value object holding a fully-qualified Java class name.
 */
@Immutable
@SuppressWarnings("ClassWithTooManyMethods")
public final class ClassName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * Separates class name from package.
     */
    private static final char DOT_SEPARATOR = '.';

    /**
     * Separates nested class name from the name of the outer class in a fully qualified name.
     */
    private static final char OUTER_CLASS_DELIMITER = '$';

    private static final String GRPC_POSTFIX = "Grpc";

    private ClassName(@BinaryName String value) {
        super(checkNotNull(value));
    }

    /**
     * Creates a new instance with the name of the passed class.
     *
     * @param cls
     *         the class to get name from
     * @return new instance
     */
    public static ClassName of(Class<?> cls) {
        checkNotNull(cls);
        return new ClassName(cls.getName());
    }

    /**
     * Creates a new instance with the passed class name value.
     *
     * @param className
     *         a fully-qualified Java class name
     * @return new
     */
    public static ClassName of(@FullyQualifiedName String className) {
        checkNotEmptyOrBlank(className);
        return new ClassName(className);
    }

    /**
     * Creates a class name from the specified package the and simple name.
     *
     * @param packageName
     *         the name of the class package
     * @param simpleClassName
     *         the simple name of the class
     * @return a new instance
     */
    public static ClassName of(PackageName packageName, SimpleClassName simpleClassName) {
        checkNotNull(packageName);
        checkNotNull(simpleClassName);
        return new ClassName(packageName.value() + DOT_SEPARATOR + simpleClassName);
    }

    /**
     * Obtains a {@code ClassName} for the outer class of the given Protobuf file.
     *
     * @param file
     *         the file from which the outer class is generated
     * @return new instance of {@code ClassName}
     */
    public static ClassName outerClass(FileDescriptor file) {
        var packageName = PackageName.resolve(file.toProto());
        var simpleName = SimpleClassName.outerOf(file);
        return of(packageName, simpleName);
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf message type descriptor.
     *
     * <p>The resulting class name is the name of the Java class that represents
     * the given Protobuf type.
     *
     * @param messageType
     *         the Protobuf message type descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(Descriptor messageType) {
        checkNotNull(messageType);
        return construct(messageType.getFile(), messageType.getName(),
                         messageType.getContainingType());
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf enum type descriptor.
     *
     * <p>The resulting class name is the name of the Java enum that represents the given Protobuf
     * type.
     *
     * @param enumType
     *         the Protobuf enum type descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(EnumDescriptor enumType) {
        return construct(enumType.getFile(), enumType.getName(),
                         enumType.getContainingType());
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf service descriptor.
     *
     * <p>The resulting class name is the name of the Java gRPC stub class that is generated from
     * the given service type.
     *
     * @param serviceType
     *         the gRPC service descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(ServiceDescriptor serviceType) {
        var packageName = PackageName.resolve(serviceType.getFile().toProto());
        var simpleName = SimpleClassName.create(serviceType.getName() + GRPC_POSTFIX);
        return of(packageName, simpleName);
    }

    private static String javaPackageName(FileDescriptor file) {
        var packageName = PackageName.resolve(file.toProto());
        return packageName.value() + DOT_SEPARATOR;
    }

    /**
     * Obtains outer class prefix, if the file has {@code java_multiple_files} set to {@code false}.
     * If the option is set, returns an empty string.
     */
    private static String outerClassPrefix(FileDescriptor file) {
        checkNotNull(file);
        var multipleFiles = file.getOptions()
                                .getJavaMultipleFiles();
        if (multipleFiles) {
            return "";
        } else {
            var className = SimpleClassName.outerOf(file)
                                           .value();
            return className + OUTER_CLASS_DELIMITER;
        }
    }

    /**
     * Obtains prefix for a type that is enclosed into the passed message.
     * If a {@code null} value is passed, returns an empty string.
     */
    private static String containingClassPrefix(@Nullable Descriptor containingMessage) {
        if (containingMessage == null) {
            return "";
        }
        Deque<String> parentClassNames = newLinkedList();
        var current = containingMessage;
        while (current != null) {
            parentClassNames.addFirst(current.getName() + OUTER_CLASS_DELIMITER);
            current = current.getContainingType();
        }
        var result = String.join("", parentClassNames);
        return result;
    }

    /**
     * Obtains the name of a nested class.
     *
     * @param className
     *         the name of the nested class to get
     * @return the nested class name
     */
    public ClassName withNested(SimpleClassName className) {
        checkNotNull(className);
        return of(value() + OUTER_CLASS_DELIMITER + className);
    }

    /**
     * Converts the name that may be a nested class name with {@link #OUTER_CLASS_DELIMITER}
     * to the name separated with dots.
     *
     * @see Class#getCanonicalName()
     */
    public String canonicalName() {
        var withDots = toDotted(value());
        return withDots;
    }

    /**
     * Obtains the binary name of the class.
     *
     * <p>Note that the retrieved value may not adhere to the JDK binary name specification.
     * The actual returned value is obtained from {@link Class#getName()}. In most cases,
     * the {@code Class.getName()} and the JDK-spec binary name coincide.
     *
     * @return the name with the {@link #OUTER_CLASS_DELIMITER}s between nested classed if any
     * @implSpec This method returns the same value as does the {@code value()} method. Use
     *         this method for more clarity in the client code.
     */
    public @ClassGetName String binaryName() {
        return value();
    }

    /**
     * Replaces {@link #OUTER_CLASS_DELIMITER} with {@link #DOT_SEPARATOR}.
     */
    @Internal
    public static String toDotted(String outerDelimited) {
        var result = outerDelimited.replace(OUTER_CLASS_DELIMITER, DOT_SEPARATOR);
        return result;
    }

    /**
     * Obtains the name of the {@link com.google.protobuf.MessageOrBuilder} interface for this
     * message class.
     *
     * <p>If this class name is {@code com.acme.cms.Customer}, the resulting class name would be
     * {@code com.acme.cms.CustomerOrBuilder}.
     *
     * <p>If this class name is {@linkplain #canonicalName() dotted}, then the resulting
     * name is dotted too.
     *
     * @return {@code MessageOrBuilder} interface FQN
     */
    public ClassName orBuilder() {
        return of(value() + OR_BUILDER_SUFFIX);
    }

    private static ClassName construct(FileDescriptor file,
                                       String typeName,
                                       @Nullable Descriptor enclosing) {
        var packageName = javaPackageName(file);
        var outerClass = outerClassPrefix(file);
        var enclosingTypes = containingClassPrefix(enclosing);
        var result = packageName + outerClass + enclosingTypes + typeName;
        return of(result);
    }

    /**
     * Converts fully qualified name to simple name. If the class is nested inside one or more
     * classes, the most nested name will be returned.
     */
    public SimpleClassName toSimple() {
        var fullName = canonicalName();
        var result = afterDot(fullName);
        return SimpleClassName.create(result);
    }

    /**
     * Obtains this class name without the package qualifier.
     *
     * <p>The result is always {@linkplain #canonicalName() dotted}.
     *
     * @return this class name without the package
     */
    @Internal
    public String withoutPackage() {
        return toDotted(afterDot(value()));
    }

    /**
     * Obtain the part of the name after the last {@link #DOT_SEPARATOR .} (dot) symbol.
     *
     * @param fullName
     *         a full class name
     * @return the last part of the name
     */
    private static String afterDot(String fullName) {
        var lastDotIndex = fullName.lastIndexOf(DOT_SEPARATOR);
        return fullName.substring(lastDotIndex + 1);
    }

    /**
     * Obtains the name of the package of this class.
     */
    public PackageName packageName() {
        var packageEndIndex = packageEndIndex();
        var result = value().substring(0, packageEndIndex);
        return PackageName.of(result);
    }

    private int packageEndIndex() {
        var fullName = value();
        var lastDotIndex = fullName.lastIndexOf(DOT_SEPARATOR);
        checkState(lastDotIndex > 0, "%s should be qualified.", fullName);
        return lastDotIndex;
    }

    /**
     * Obtains the simple name of the top level class.
     *
     * <p>If this class is top level, returns the simple name of this class.
     * If this class is nested, returns the name of the declaring top level class.
     */
    public SimpleClassName topLevelClass() {
        var qualifiedClassName = afterDot(value());
        var delimiterIndex = qualifiedClassName.indexOf(OUTER_CLASS_DELIMITER);
        var topLevelClassName = delimiterIndex >= 0
                                   ? qualifiedClassName.substring(0, delimiterIndex)
                                   : qualifiedClassName;
        return SimpleClassName.create(topLevelClassName);
    }
}
