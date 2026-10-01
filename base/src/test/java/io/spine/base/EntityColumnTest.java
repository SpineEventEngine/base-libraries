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

package io.spine.base;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Timestamp;
import io.spine.base.given.FakeEntityState;
import io.spine.query.EntityColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`EntityColumn` should")
class EntityColumnTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .setDefault(String.class, "non-empty-column-name")
                .testAllPublicConstructors(EntityColumn.class);
    }

    @Test
    @DisplayName("expose the its attributes")
    void exposeColumnName() {
        var columnName = "some-column";
        var returningType = Timestamp.class;
        var column = new EntityColumn<FakeEntityState, Timestamp>(columnName, returningType,
                                                                  (r) -> Time.currentTime());
        assertThat(column.name().value()).isEqualTo(columnName);
        assertThat(column.type()).isEqualTo(returningType);
    }
}
