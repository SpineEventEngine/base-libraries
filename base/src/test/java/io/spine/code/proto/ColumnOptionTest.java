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

import com.google.common.testing.NullPointerTester;
import io.spine.test.code.proto.CoProject;
import io.spine.test.code.proto.CoTask;
import io.spine.test.code.proto.CoTaskDescription;
import io.spine.type.MessageType;
import io.spine.value.StringTypeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`ColumnOption` should")
class ColumnOptionTest {

    private final MessageType type = new MessageType(CoProject.getDescriptor());

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ColumnOption.class);
    }

    @Test
    @DisplayName("determine if the message type has columns")
    void checkHasColumns() {
        assertThat(ColumnOption.hasColumns(type)).isTrue();
    }

    @Test
    @DisplayName("determine that message type has no columns")
    void checkHasNoColumns() {
        var typeWithoutColumns = new MessageType(CoTask.getDescriptor());
        assertThat(ColumnOption.hasColumns(typeWithoutColumns)).isFalse();
    }

    @Test
    @DisplayName("determine that message type is not eligible for having columns")
    void checkNotEligibleForColumns() {
        var nonEligible = new MessageType(CoTaskDescription.getDescriptor());
        assertThat(ColumnOption.hasColumns(nonEligible)).isFalse();
    }

    @Test
    @DisplayName("obtain columns of the entity")
    void obtainColumns() {
        var columns = ColumnOption.columnsOf(type);
        assertThat(columns).hasSize(2);

        var columnNames = columns.stream()
                .map(FieldDeclaration::name)
                .map(StringTypeValue::value)
                .collect(toImmutableList());
        assertThat(columnNames).containsExactly("name", "estimate");
    }

    @Test
    @DisplayName("return empty list of columns if the message is not eligible for having ones")
    void obtainEmptyColumns() {
        var nonEligible = new MessageType(CoTaskDescription.getDescriptor());
        var list = ColumnOption.columnsOf(nonEligible);
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("determine that the passed field is a column")
    void checkIsColumn() {
        var nameField = fieldByName("name");
        var isColumn = ColumnOption.isColumn(nameField);
        assertThat(isColumn).isTrue();
    }

    @Test
    @DisplayName("determine that the passed field is not a column")
    void checkIsNotColumn() {
        var statusField = fieldByName("status");
        var isColumn = ColumnOption.isColumn(statusField);
        assertThat(isColumn).isFalse();
    }

    @Test
    @DisplayName("return `false` for fields of type non-eligible for having columns")
    void checkFieldOfNonEligible() {
        var descriptor = CoTaskDescription.getDescriptor()
                                          .findFieldByName("value");
        var field = new FieldDeclaration(descriptor);
        var isColumn = ColumnOption.isColumn(field);
        assertThat(isColumn).isFalse();
    }

    private FieldDeclaration fieldByName(String name) {
        var field = type.descriptor()
                        .findFieldByName(name);
        var result = new FieldDeclaration(field);
        return result;
    }
}
