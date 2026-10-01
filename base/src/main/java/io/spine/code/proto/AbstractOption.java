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
import com.google.errorprone.annotations.ImmutableTypeParameter;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.GeneratedMessage.ExtendableMessage;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.util.Optional;

/**
 * An abstract base for types of Protobuf options such as {@link FieldOption} and
 * {@link MessageOption}.
 *
 * @param <T>
 *         the type of value held by this option
 * @param <K>
 *         the type of values that this option is applied to
 * @param <E>
 *         the type of object that holds all options of {@code K}
 */
@Immutable
public abstract class AbstractOption<@ImmutableTypeParameter T,
                                     @ImmutableTypeParameter K extends GenericDescriptor,
                                     @ImmutableTypeParameter E extends ExtendableMessage<E>>
        implements Option<T, K> {

    @SuppressWarnings("Immutable") // effectively
    private final GeneratedExtension<E, T> extension;

    /** Creates a new instance of the option using the specified extension. */
    AbstractOption(GeneratedExtension<E, T> extension) {
        this.extension = extension;
    }

    /**
     * Returns an option object of the specified {@code K}.
     *
     * <p>Examples of option objects include
     * {@link com.google.protobuf.DescriptorProtos.FieldOptions} for fields,
     * {@link com.google.protobuf.DescriptorProtos.FileOptions} for files, etc.
     */
    protected abstract E optionsFrom(K object);

    /**
     * Returns the extension that represents this option.
     */
    public GeneratedExtension<E, T> extension() {
        return extension;
    }

    @Override
    public Optional<T> valueFrom(K object) {
        var options = optionsFrom(object);
        return options.hasExtension(extension)
               ? Optional.of(options.getExtension(this.extension))
               : Optional.empty();

    }

    @Override
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractOption<?, ?, ?> option)) {
            return false;
        }
        return extension.getNumber() == option.extension.getNumber();
    }

    /**
     * Computes the hash code of this option basing on its extension number.
     *
     * <p>Improves the performance of operations that group the {@code AbstractOption} instances
     * into {@code Set}s or other structures relying on the hash code value.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return extension.getNumber();
    }
}
