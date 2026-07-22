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
import io.spine.option.EntityOption;
import io.spine.option.OptionsProto;

import java.util.Optional;

/**
 * An option for a message representing a state of the entity that defines its kind and visibility
 * to queries.
 *
 * <p>There are four kinds of options, namely, Aggregate, Projection, Process Manager, and Entity.
 */
@Immutable
public final class EntityStateOption extends MessageOption<EntityOption> {

    private EntityStateOption() {
        super(OptionsProto.entity);
    }

    /**
     * Obtains the value of the {@code (entity)} option from the specified message.
     *
     * @param message
     *         the message to obtain the option value from
     * @return either an {@code Optional} containing the value of the {@code (entity)} option
     *         or an empty {@code Optional}
     * @apiNote This method is just a shorthand for
     *        <pre>
     *        EntityStateOption option = new EntityStateOption();
     *        option.valueFrom(messageDescriptor);
     *        </pre>
     *        to avoid instantiating an object.
     */
    public static Optional<EntityOption> valueOf(Descriptor message) {
        var option = new EntityStateOption();
        return option.valueFrom(message);
    }

    /**
     * Obtains an entity kind of the message as defined by the {@code (entity)} option.
     *
     * @return an {@code Optional} containing the entity kind if the option is present and an empty
     *         {@code Optional} otherwise
     */
    public static Optional<EntityOption.Kind> entityKindOf(Descriptor message) {
        var option = valueOf(message);
        return option.map(EntityOption::getKind);
    }
}
