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

package io.spine.protobuf;

import com.google.protobuf.EnumValue;
import com.google.protobuf.ProtocolMessageEnum;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Handles conversion of Java {@link Enum} objects to respective {@link EnumValue} Protobuf
 * counterpart.
 *
 * <p>Enums are converted by their {@linkplain EnumValue#getName() name} first or by the
 * {@linkplain EnumValue#getNumber() number} if the name is not present.
 */
final class EnumConverter extends ProtoConverter<EnumValue, Enum<? extends ProtocolMessageEnum>> {

    /**
     * A special name of a Protobuf enum entry that denotes an unrecognized value.
     */
    private static final String UNRECOGNIZED_PROTO_ENUM = "UNRECOGNIZED";

    private final Class<? extends Enum<? extends ProtocolMessageEnum>> type;

    /**
     * Creates a new converter for the specified {@code type}.
     */
    EnumConverter(Class<? extends Enum<? extends ProtocolMessageEnum>> type) {
        super();
        this.type = checkNotNull(type);
    }

    @Override
    protected Enum<? extends ProtocolMessageEnum> toObject(EnumValue input) {
        var name = input.getName();
        if (name.isEmpty()) {
            var number = input.getNumber();
            return findByNumber(number);
        } else {
            return findByName(name);
        }
    }

    /**
     * Retrieves the enum constant with the specified {@code number}.
     *
     * @throws IllegalArgumentException
     *         if enum constant with such a number is not present
     */
    private Enum<? extends ProtocolMessageEnum> findByNumber(int number) {
        var constants = type.getEnumConstants();
        for (var constant : constants) {
            var isUnrecognized = isUnrecognized(constant);
            if (isUnrecognized && number == -1) {
                return constant;
            }
            if (isUnrecognized) {
                continue;
            }
            var asProtoEnum = (ProtocolMessageEnum) constant;
            var valueNumber = asProtoEnum.getNumber();
            if (number == valueNumber) {
                return constant;
            }
        }
        throw unknownNumber(number);
    }

    private static boolean isUnrecognized(Enum<? extends ProtocolMessageEnum> constant) {
        return UNRECOGNIZED_PROTO_ENUM.equalsIgnoreCase(constant.name());
    }

    /**
     * {@linkplain Enum#valueOf(Class, String) Retrieves} the enum constant with
     * the specified {@code name}.
     *
     * @throws IllegalArgumentException
     *         if enum constant with such a name is not present
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // Checked at runtime.
    private Enum<? extends ProtocolMessageEnum> findByName(String name) {
        var result = Enum.valueOf((Class<? extends Enum>) type, name);
        return (Enum<? extends ProtocolMessageEnum>) result;
    }

    @Override
    protected EnumValue toMessage(Enum<? extends ProtocolMessageEnum> input) {
        var name = input.name();
        var asProtoEnum = (ProtocolMessageEnum) input;
        var value = EnumValue.newBuilder()
                .setName(name)
                .setNumber(asProtoEnum.getNumber())
                .build();
        return value;
    }

    private IllegalArgumentException unknownNumber(int number) {
        throw newIllegalArgumentException(
                "Could not find a enum value of type `%s` for number `%d`.",
                type.getCanonicalName(), number
        );
    }
}
