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
