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

import com.google.common.collect.ImmutableList;
import io.spine.option.OptionsProto;
import io.spine.type.MessageType;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * An option that marks entity state fields as entity columns.
 *
 * <p>Such fields are stored separately from the entity record and can be specified as criteria for
 * the entity query filters.
 *
 * <p>See the Protobuf option for details.
 */
public final class ColumnOption extends FieldOption<Boolean> {

    /**
     * Prevents instantiation from outside.
     *
     * <p>Use the static methods of this class to extract the column values.
     */
    private ColumnOption() {
        super(OptionsProto.column);
    }

    /**
     * Returns {@code true} if the specified message type has at least one declared column.
     *
     * <p>If the message type is not eligible for having columns, returns {@code false} regardless
     * of how fields are declared.
     */
    public static boolean hasColumns(MessageType messageType) {
        if (!declaresEntity(messageType)) {
            return false;
        }
        var result = messageType.fields().stream()
                .anyMatch(ColumnOption::isColumn);
        return result;
    }

    /**
     * Returns all fields of a message type that are declared as columns.
     *
     * <p>If the message type is not eligible for having columns, returns an empty list regardless
     * of how fields are declared.
     */
    public static ImmutableList<FieldDeclaration> columnsOf(MessageType messageType) {
        if (!declaresEntity(messageType)) {
            return ImmutableList.of();
        }
        var result = messageType.fields().stream()
                .filter(ColumnOption::isColumn)
                .collect(toImmutableList());
        return result;
    }

    /**
     * Returns {@code true} if the specified field is an entity column.
     *
     * <p>If the declaring message type is not eligible for having columns, returns {@code false}
     * regardless of how the field is declared.
     *
     * <p>The {@code repeated} and {@code map} fields cannot be columns.
     */
    public static boolean isColumn(FieldDeclaration field) {
        if (!declaresEntity(field.declaringType())) {
            return false;
        }
        if (field.isCollection()) {
            return false;
        }
        var option = new ColumnOption();
        var value = option.valueFrom(field.descriptor());
        boolean isColumn = value.orElse(false);
        return isColumn;
    }

    /**
     * Returns {@code true} if the given message type is declared as an entity and may have columns.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // For readability.
    private static boolean declaresEntity(MessageType messageType) {
        var entityOption = EntityStateOption.valueOf(messageType.descriptor());
        return entityOption.isPresent();
    }
}
