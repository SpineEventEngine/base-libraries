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

package io.spine.query;

import com.google.common.testing.NullPointerTester;
import io.spine.code.proto.FieldDeclaration;
import io.spine.test.code.proto.CoProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ColumnName` should")
class ColumnNameTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ColumnName.class);
    }

    @Test
    @DisplayName("be constructed from string value")
    void initFromString() {
        var columnName = "the-column-name";
        var name = ColumnName.of(columnName);

        assertThat(name.value()).isEqualTo(columnName);
    }

    @Test
    @DisplayName("not be constructed from empty string")
    @SuppressWarnings({"CheckReturnValue",
            "ResultOfMethodCallIgnored" /* Called to trigger the exception. */ })
    void notInitFromEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ColumnName.of(""));
    }

    @Test
    @DisplayName("be constructed from `FieldDeclaration`")
    void initFromFieldDeclaration() {
        var field = CoProject.getDescriptor()
                             .getFields()
                             .get(0);
        var fieldDeclaration = new FieldDeclaration(field);
        var columnName = ColumnName.of(fieldDeclaration);

        assertThat(columnName.value()).isEqualTo(field.getName());
    }
}
