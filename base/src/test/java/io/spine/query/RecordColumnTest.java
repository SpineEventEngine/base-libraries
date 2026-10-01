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

import io.spine.testing.TestValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.nullRef;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`RecordColumn` should")
class RecordColumnTest {

    @Test
    @DisplayName("allow creating new instances")
    void allowCreation() {
        var name = "description";
        var description = "some description";
        var column = new RecordColumn<Manufacturer, String>(name, String.class, (r) -> description);

        assertThat(column).isNotNull();
        assertThat(column.name().value()).isEqualTo(name);
        assertThat(column.type()).isEqualTo(String.class);
        assertThat(column.valueIn(Manufacturer.getDefaultInstance())).isEqualTo(description);
    }

    @Nested
    @DisplayName("prevent from passing")
    final class Prevent {

        @Test
        @DisplayName("empty or `null` column name into ctor")
        void emptyOrNullColumnName() {
            assertThrows(IllegalArgumentException.class,
                         () -> new RecordColumn<>("", String.class, (r) -> ""));

            assertThrows(NullPointerException.class,
                         () -> new RecordColumn<Manufacturer, String>(TestValues.<String>nullRef(),
                                                                      String.class,
                                                                      (r) -> ""));
        }

        @Test
        @DisplayName("`null` value type into ctor")
        void nullValueType() {
            assertThrows(NullPointerException.class,
                         () -> new RecordColumn<Manufacturer, String>("isin",
                                                                      nullRef(),
                                                                      (r) -> ""));
        }

        @Test
        @DisplayName("`null` getter into ctor")
        void nullGetter() {
            assertThrows(NullPointerException.class,
                         () -> new RecordColumn<Manufacturer, String>("isin",
                                                                      String.class,
                                                                      nullRef()));
        }
    }
}
