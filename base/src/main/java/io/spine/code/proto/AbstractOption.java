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
