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

import io.spine.annotation.GeneratedMixin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("GeneratedMixin annotation should")
class GeneratedMixinTest {

    @Test
    @DisplayName("have `SOURCE` retention policy")
    void retention() {
       assertThat(annotation(Retention.class).value())
               .isEqualTo(RetentionPolicy.SOURCE);
    }

    @Test
    @DisplayName("have `TYPE` target")
    void target() {
        var assertTargets = assertThat(annotation(Target.class).value()).asList();

        assertTargets.hasSize(1);
        assertTargets.contains(ElementType.TYPE);
    }

    @Test
    @DisplayName("be `Documented`")
    void documented() {
        assertThat(annotation(Documented.class)).isNotNull();
    }

    @Test
    @DisplayName("be `Inherited`")
    void inherited() {
        assertThat(annotation(Inherited.class)).isNotNull();
    }

    private static <A extends Annotation> A annotation(Class<A> annotationClass) {
        return GeneratedMixin.class.getAnnotation(annotationClass);
    }
}
