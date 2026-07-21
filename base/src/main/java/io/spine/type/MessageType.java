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

package io.spine.type;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import io.spine.base.UuidValue;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.LocationPath;
import io.spine.code.proto.TypeSet;
import io.spine.option.OptionsProto;

import java.util.Deque;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Streams.concat;
import static io.spine.code.proto.EntityStateOption.entityKindOf;
import static io.spine.code.proto.FileDescriptors.sameFiles;
import static io.spine.option.EntityOption.Kind.KIND_UNKNOWN;
import static io.spine.option.EntityOption.Kind.UNRECOGNIZED;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A message type as declared in a proto file.
 */
@Immutable
@SuppressWarnings("ClassWithTooManyMethods")
public class MessageType extends Type<Descriptor, DescriptorProto> {

    private final ImmutableList<FieldDeclaration> fields;

    /**
     * Creates a new instance by the given message descriptor.
     */
    public MessageType(Descriptor descriptor) {
        super(descriptor, true);
        this.fields = collectFields();
    }

    private ImmutableList<FieldDeclaration> collectFields() {
        var descriptors = descriptor().getFields();
        var fields = ImmutableList.<FieldDeclaration>builderWithExpectedSize(descriptors.size());
        for (var field : descriptors) {
            fields.add(new FieldDeclaration(field, this));
        }
        return fields.build();
    }

    /**
     * Obtains the type of the given message.
     */
    public static MessageType of(Message message) {
        checkNotNull(message);
        return new MessageType(message.getDescriptorForType());
    }

    /**
     * Collects all message types, including nested one, declared in the given file.
     */
    public static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        var result = TypeSet.newBuilder();
        for (var messageType : file.getMessageTypes()) {
            addType(messageType, result);
        }
        return result.build();
    }

    private static void addType(Descriptor type, TypeSet.Builder set) {
        if (type.getOptions()
                .getMapEntry()) {
            return;
        }
        var messageType = new MessageType(type);
        set.add(messageType);
        for (var nestedType : type.getNestedTypes()) {
            addType(nestedType, set);
        }
    }

    @Override
    public final DescriptorProto toProto() {
        return descriptor().toProto();
    }

    @Override
    public final TypeUrl url() {
        return TypeUrl.from(descriptor());
    }

    @Override
    public final ClassName javaClassName() {
        return ClassName.from(descriptor());
    }

    @Override
    public Optional<Type<Descriptor, DescriptorProto>> containingType() {
        var parent = descriptor().getContainingType();
        return Optional.ofNullable(parent)
                       .map(MessageType::new);
    }

    @Override
    @SuppressWarnings("unchecked") // It is safe since we work with a message descriptors
    public Class<? extends Message> javaClass() {
        return (Class<? extends Message>) super.javaClass();
    }

    /**
     * Tells if this message is under the "google" package.
     */
    public boolean isGoogle() {
        var result = FileDescriptors.isGoogle(file());
        return result;
    }

    /**
     * Tells if this message type is not from "google" package, and is not an extension
     * defined in "options.proto".
     */
    public boolean isCustom() {
        if (isGoogle()) {
            return false;
        }
        var optionsProto = OptionsProto.getDescriptor();
        var file = file();
        var result = !sameFiles(optionsProto, file);
        return result;
    }

    /**
     * Tells if this message is top-level in its file.
     */
    public boolean isTopLevel() {
        var descriptor = descriptor();
        return isTopLevel(descriptor);
    }

    /**
     * Verifies if the message is top-level (rather than nested).
     */
    public static boolean isTopLevel(Descriptor descriptor) {
        var parent = descriptor.getContainingType();
        return parent == null;
    }

    /**
     * Tells if this message is nested inside another message declaration.
     */
    public boolean isNested() {
        return !isTopLevel();
    }

    /**
     * Tells if this message is a rejection.
     */
    public boolean isRejection() {
        var result = isTopLevel() && declaringFileName().isRejections();
        return result;
    }

    /**
     * Tells if this message is a command.
     */
    public boolean isCommand() {
        var result = isTopLevel() && declaringFileName().isCommands();
        return result;
    }

    /**
     * Tells if this message is an event.
     *
     * <p>Returns {@code false} if this type is a {@linkplain #isRejection() rejection}.
     */
    public boolean isEvent() {
        var result = isTopLevel() && declaringFileName().isEvents();
        return result;
    }

    /**
     * Tells if this message is a signal.
     */
    public boolean isSignal() {
        return isCommand() || isEvent() || isRejection();
    }

    /**
     * Tells if this message is an entity state.
     */
    public boolean isEntityState() {
        var entityKind = entityKindOf(descriptor());
        var result = entityKind.isPresent()
                && entityKind.get() != UNRECOGNIZED
                && entityKind.get() != KIND_UNKNOWN;
        return result;
    }

    /**
     * Obtains all nested declarations that match the passed predicate.
     */
    public ImmutableList<MessageType> nestedTypesThat(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageType> result = ImmutableList.builder();
        Iterable<MessageType> nestedDeclarations = immediateNested();
        Deque<MessageType> deque = newLinkedList(nestedDeclarations);

        while (!deque.isEmpty()) {
            var nestedDeclaration = deque.pollFirst();

            assert nestedDeclaration != null; // Cannot be null since the queue is not empty.
            var nestedDescriptor = nestedDeclaration.descriptor().toProto();

            if (predicate.test(nestedDescriptor)) {
                result.add(nestedDeclaration);
            }

            deque.addAll(nestedDeclaration.immediateNested());
        }
        return result.build();
    }

    /**
     * Obtains immediate declarations of nested types of this declaration, or
     * empty list if no nested types are declared.
     */
    private ImmutableList<MessageType> immediateNested() {
        var descriptors = descriptor().getNestedTypes();
        var result = descriptors.stream()
                .map(MessageType::new)
                .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains all the nested message and enum declarations.
     *
     * <p>Includes only the immediate declarations. Types declared inside the types declared inside
     * this type are not obtained.
     */
    public ImmutableList<Type<?, ?>> nestedDeclarations() {
        var nestedDescriptors = descriptor().getNestedTypes();
        Stream<Type<?, ?>> messageTypes = nestedDescriptors.stream().map(MessageType::new);
        var enumDescriptors = descriptor().getEnumTypes();
        Stream<Type<?, ?>> enumTypes = enumDescriptors.stream().map(EnumType::create);
        return concat(messageTypes, enumTypes)
                .collect(toImmutableList());
    }

    /**
     * Obtains fields declared in the message type.
     */
    public ImmutableList<FieldDeclaration> fields() {
        return fields;
    }

    /**
     * Finds a {@linkplain FieldDeclaration declaration} of a field by the field name.
     *
     * <p>Throws an {@link IllegalArgumentException} if a field with such a name is not declared in
     * this message type.
     *
     * @param name the name of a Protobuf field
     * @return the field declaration
     */
    public FieldDeclaration field(String name) {
        var fieldDescriptor = descriptor().findFieldByName(name);
        checkArgument(fieldDescriptor != null, name);
        return new FieldDeclaration(fieldDescriptor, this);
    }

    /**
     * Returns the message location path for a top-level message definition.
     *
     * @return the message location path
     */
    public LocationPath path() {
        var result = LocationPath.fromMessage(descriptor());
        return result;
    }

    /**
     * Obtains the comments going before a rejection declaration.
     *
     * <p>Requires the following Protobuf plugin configuration:
     * <pre> {@code
     * generateProtoTasks {
     *     all().configureEach {
     *         // If true, the descriptor set will contain line number information
     *         // and comments. Default is false.
     *         descriptorSetOptions.includeSourceInfo = true
     *         // ...
     *     }
     * }
     * }</pre>
     *
     * @return the comments text or {@code Optional.empty()} if there are no comments
     * @see <a href="https://github.com/google/protobuf-gradle-plugin/blob/master/README.md#generate-descriptor-set-files">
     *         Protobuf plugin configuration</a>
     */
    public Optional<String> leadingComments() {
        var messagePath = path();
        return leadingComments(messagePath);
    }

    /**
     * Obtains the leading comments for the {@link LocationPath}.
     *
     * <p>This method logs a warning message if the file declaring this message type does not have
     * {@linkplain FileDescriptorProto#hasSourceCodeInfo() source code info}
     * produced by Protobuf compiler. The warning message provides instructions for configuring
     * the Protobuf Gradle plugin for enabling source code info.
     *
     * @param locationPath
     *         the location path to get leading comments
     * @return the leading comments or empty {@code Optional} if there are no such comments or
     *         a descriptor was generated without source code information
     */
    public Optional<String> leadingComments(LocationPath locationPath) {
        var file = fileDescriptorProto();
        if (!file.hasSourceCodeInfo()) {
            warnNoSourceCodeInfoIn(file);
            return Optional.empty();
        }

        var location = locationPath.toLocationIn(file);
        return location.hasLeadingComments()
               ? Optional.of(location.getLeadingComments())
               : Optional.empty();
    }

    /**
     * Obtains a file descriptor via {@link KnownTypes} because it loads a descriptor set file,
     * which may contain source code information.
     *
     * <p>Descriptors available in the generated code -- obtained via
     * {@code descriptor().getFile().toProto()} -- do not provide source code info.
     */
    private FileDescriptorProto fileDescriptorProto() {
        return name().messageDescriptor()
                     .getFile()
                     .toProto();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr") /* We want to avoid dependency on logging
        in this part of code which is going to be deprecated eventually. */
    private void warnNoSourceCodeInfoIn(FileDescriptorProto file) {
        System.err.printf(
                "Unable to obtain proto source code info for the message type `%s`" +
                        " declared in the file `%s`.%n" +
                        "Please configure the Gradle Protobuf plugin as follows:" +
                        "%n%n" +
                        "protobuf {%n" +
                        "    generateProtoTasks.all().configureEach {%n" +
                        "        descriptorSetOptions.includeSourceInfo = true %n" +
                        "    }%n" +
                        "}%n",
                name(),
                file.getName());
    }

    /**
     * Determines if the message type represents a {@link UuidValue}.
     */
    public boolean isUuidValue() {
        var fields = fields();
        if (fields.size() != 1) {
            return false;
        }
        var theField = fields.get(0);
        var uuid = FieldName.of("uuid");
        var nameMatches = uuid.equals(theField.name());
        var typeMatches = theField.isString();
        return nameMatches && typeMatches;
    }

    /**
     * Checks if the message has the given option.
     *
     * <p>The option must be known at runtime. An uninterpreted option cannot be found this way.
     *
     * @param optionName
     *         name of the option
     * @return {@code true} if this type is marked with an option with the given name,
     *         {@code false} otherwise
     */
    public boolean hasOption(String optionName) {
        checkNotEmptyOrBlank(optionName,"Option name must not be null empty.");
        var options = descriptor().getOptions();
        var presentOptions = options.getAllFields().keySet();
        return presentOptions.stream()
                             .anyMatch(op -> optionName.equals(op.getName()));
    }
}
