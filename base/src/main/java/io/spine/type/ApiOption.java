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

package io.spine.type;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.GeneratedMessage.ExtendableMessage;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.Descriptors.Descriptor;
import static com.google.protobuf.Descriptors.FieldDescriptor;
import static io.spine.option.OptionsProto.beta;
import static io.spine.option.OptionsProto.betaAll;
import static io.spine.option.OptionsProto.betaType;
import static io.spine.option.OptionsProto.experimental;
import static io.spine.option.OptionsProto.experimentalAll;
import static io.spine.option.OptionsProto.experimentalType;
import static io.spine.option.OptionsProto.internal;
import static io.spine.option.OptionsProto.internalAll;
import static io.spine.option.OptionsProto.internalType;
import static io.spine.option.OptionsProto.sPIAll;
import static io.spine.option.OptionsProto.sPIService;
import static io.spine.option.OptionsProto.sPIType;

/**
 * A set of Protobuf options marking an API element.
 *
 * <p>The options in a single set have different targets (such as files, message types, fields,
 * etc.), but always represent a single concept in terms of API visibility, status, etc.
 *
 * <p>For example, options {@code (beta)}, {@code (beta_type)}, and {@code (beta_all)} target,
 * respectively, fields, messages, and files, but all represent a single concept -
 * a <strong>beta API</strong> element.
 */
@Immutable
public final class ApiOption {

    /*
     * Fields of type `GeneratedExtension` are immutable, despite not being annotated.
     */

    @SuppressWarnings("Immutable")
    private final GeneratedExtension<FileOptions, Boolean> fileOption;
    @SuppressWarnings("Immutable")
    private final GeneratedExtension<MessageOptions, Boolean> messageOption;
    @SuppressWarnings("Immutable")
    private final @Nullable GeneratedExtension<ServiceOptions, Boolean> serviceOption;
    @SuppressWarnings("Immutable")
    private final @Nullable GeneratedExtension<FieldOptions, Boolean> fieldOption;

    private ApiOption(GeneratedExtension<FileOptions, Boolean> fileOption,
                      GeneratedExtension<MessageOptions, Boolean> messageOption,
                      @Nullable GeneratedExtension<ServiceOptions, Boolean> serviceOption,
                      @Nullable GeneratedExtension<FieldOptions, Boolean> fieldOption) {
        this.fileOption = checkNotNull(fileOption);
        this.messageOption = checkNotNull(messageOption);
        this.serviceOption = serviceOption;
        this.fieldOption = fieldOption;
    }

    /**
     * Enumeration of available API options.
     *
     * @implNote This private enum gathers option instances in the form that is more
     *         compact than a bunch of private static fields.
     */
    private enum The {

        BETA(new ApiOption(betaAll, betaType, null, beta)),
        EXPERIMENTAL(new ApiOption(experimentalAll, experimentalType, null, experimental)),
        INTERNAL(new ApiOption(internalAll, internalType, null, internal)),
        SPI(new ApiOption(sPIAll, sPIType, sPIService, null));

        @SuppressWarnings("PMD.SingularField")
        // This private enum should not be serialized.
        private final ApiOption option;

        The(ApiOption option) {
            this.option = option;
        }
    }

    /**
     * Obtains an option that marks beta API.
     */
    public static ApiOption beta() {
        return The.BETA.option;
    }

    /**
     * Obtains an option that marks experimental API.
     */
    public static ApiOption experimental() {
        return The.EXPERIMENTAL.option;
    }

    /**
     * Obtains an option that marks internal API.
     */
    public static ApiOption internal() {
        return The.INTERNAL.option;
    }

    /**
     * Obtains an option that marks beta service provider interface elements.
     */
    public static ApiOption spi() {
        return The.SPI.option;
    }

    /**
     * Checks if the given file is declared with this option.
     *
     * @param descriptor
     *         the file descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    public Optional<Boolean> findIn(FileDescriptor descriptor) {
        var options = descriptor.getOptions();
        return find(options, fileOption);
    }

    /**
     * Checks if the given message is declared with this option.
     *
     * @param descriptor
     *         the message descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    public Optional<Boolean> findIn(Descriptor descriptor) {
        var options = descriptor.getOptions();
        return find(options, messageOption);
    }

    /**
     * Checks if the given service is declared with this option.
     *
     * @param descriptor
     *         the service descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    public Optional<Boolean> findIn(ServiceDescriptor descriptor) {
        checkState(serviceOption != null, "Option %s does not support services.", this);
        var options = descriptor.getOptions();
        return find(options, serviceOption);
    }

    /**
     * Checks if the given field is declared with this option.
     *
     * @param descriptor
     *         the field descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    public Optional<Boolean> findIn(FieldDescriptor descriptor) {
        checkState(fieldOption != null, "Option %s does not support fields.", this);
        var options = descriptor.getOptions();
        return find(options, fieldOption);
    }

    /**
     * Checks if Protobuf services may be defined with this option.
     *
     * @return {@code true} if the option supports services, {@code false} otherwise
     */
    public boolean supportsServices() {
        return serviceOption != null;
    }

    /**
     * Obtains the value of the boolean option, if it exists, returning it as {@code Optional}
     * with the value.
     *
     * <p>If the option does not exist, returns {@code Optional.empty()}.
     */
    private static <T extends ExtendableMessage<T>>
    Optional<Boolean> find(ExtendableMessage<T> options, GeneratedExtension<T, Boolean> option) {
        if (!options.hasExtension(option)) {
            return Optional.empty();
        }
        var value = options.getExtension(option);
        return Optional.of(value);
    }

    /**
     * Checks if message fields may be defined with this option.
     *
     * @return {@code true} if the option supports fields, {@code false} otherwise
     */
    public boolean supportsFields() {
        return fieldOption != null;
    }

    @Override
    public String toString() {
        return messageOption.getDescriptor()
                            .getName();
    }
}
