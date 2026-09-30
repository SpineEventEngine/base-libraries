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
import com.google.errorprone.annotations.Immutable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count;

@DisplayName("`Columns` should")
class ColumnsTest {

    /**
     * Checks that {@code Columns} is immutable.
     *
     * @implNote In this test we just ensure this type is marked with {@code Immutable}.
     *         The rest is done by Error Prone.
     */
    @Test
    @DisplayName("be immutable")
    void beImmutable() {
        var declaredAnnotations = Columns.class.getDeclaredAnnotations();
        var annotationTypes = Stream.of(declaredAnnotations)
                                    .map(Annotation::annotationType)
                                    .collect(toImmutableSet());
        assertThat(annotationTypes)
                .contains(Immutable.class);
    }

    @Test
    @DisplayName("create new instances from the passed `RecordColumn`s")
    void createNewInstances() {
        var columns = Columns.of(is_traded, isin, stock_count);
        assertThat(columns)
                .containsExactly(is_traded, isin, stock_count);
    }

    @Test
    @DisplayName("not accept `null` arguments")
    void notAcceptNulls() {
        new NullPointerTester()
                .testAllPublicStaticMethods(Columns.class);
    }
}
