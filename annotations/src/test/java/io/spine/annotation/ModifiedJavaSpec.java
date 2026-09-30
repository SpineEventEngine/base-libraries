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

package io.spine.annotation;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Tests for the {@link Modified} annotation on how it is viewed from the Java API perspective.
 */
@DisplayName("`@Modified` annotation should")
class ModifiedJavaSpec {

    @Test
    @DisplayName("have `SOURCE` retention")
    void retention() {
        var retention = Modified.class.getAnnotation(Retention.class);
        assertThat(retention.value()).isEqualTo(RetentionPolicy.SOURCE);
    }

    @Test
    @DisplayName("target many things")
    void targeting() {
        var target = Modified.class.getAnnotation(Target.class);
        assertThat(target.value())
                .asList()
                .containsAtLeastElementsIn(
                        ImmutableList.of(
                                ANNOTATION_TYPE,
                                CONSTRUCTOR,
                                FIELD,
                                LOCAL_VARIABLE,
                                METHOD,
                                PARAMETER,
                                TYPE
                        )
                );
    }
}
