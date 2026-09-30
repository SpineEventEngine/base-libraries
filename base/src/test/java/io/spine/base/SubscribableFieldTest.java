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
import io.spine.query.EntityStateField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`SubscribableField` should")
class SubscribableFieldTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicConstructors(EntityStateField.class);
        new NullPointerTester()
                .testAllPublicConstructors(EventMessageField.class);
    }

    @Test
    @DisplayName("expose the field reference")
    void exposeColumnName() {
        var field = newField();
        SubscribableField subscribableField = new EntityStateField(field);
        assertThat(subscribableField.getField()).isEqualTo(field);
    }

    private static Field newField() {
        return Field.named("some-field");
    }
}
