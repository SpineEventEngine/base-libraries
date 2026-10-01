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

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.option.EntityOption.Kind;
import io.spine.option.EntityOption.Visibility;
import io.spine.test.code.proto.EsoEntity;
import io.spine.test.code.proto.EsoPublicProjection;
import io.spine.test.code.proto.EsoSecretAggregate;
import io.spine.test.code.proto.EsoSubscribablePm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.option.EntityOption.Kind.AGGREGATE;
import static io.spine.option.EntityOption.Kind.ENTITY;
import static io.spine.option.EntityOption.Kind.PROCESS_MANAGER;
import static io.spine.option.EntityOption.Kind.PROJECTION;
import static io.spine.option.EntityOption.Visibility.FULL;
import static io.spine.option.EntityOption.Visibility.NONE;
import static io.spine.option.EntityOption.Visibility.QUERY;
import static io.spine.option.EntityOption.Visibility.SUBSCRIBE;

@DisplayName("`EntityStateOption` should")
class EntityStateOptionTest {

    @Test
    @DisplayName("obtain value for an `ENTITY` kind")
    void entity() {
        assertOption(EsoEntity.getDescriptor(), ENTITY, QUERY);
    }

    @Test
    @DisplayName("obtain value for an `AGGREGATE` kind")
    void aggregate() {
        assertOption(EsoSecretAggregate.getDescriptor(), AGGREGATE, NONE);
    }

    @Test
    @DisplayName("obtain value for an `PROCESS_MANAGER` kind")
    void processManager() {
        assertOption(EsoSubscribablePm.getDescriptor(), PROCESS_MANAGER, SUBSCRIBE);
    }

    @Test
    @DisplayName("obtain value for an `PROJECTION` kind")
    void projection() {
        assertOption(EsoPublicProjection.getDescriptor(), PROJECTION, FULL);
    }

    @Test
    @DisplayName("obtain the kind of an entity")
    void obtainEntityKind() {
        var kind = EntityStateOption.entityKindOf(EsoPublicProjection.getDescriptor());
        assertThat(kind).isPresent();
        assertThat(kind.get()).isEqualTo(PROJECTION);
    }

    void assertOption(Descriptor type, Kind kind, Visibility visibility) {
        var found = EntityStateOption.valueOf(type);
        assertThat(found).isPresent();
        @SuppressWarnings("OptionalGetWithoutIsPresent") // checked above.
        var option = found.get();
        assertThat(option.getKind())
                .isEqualTo(kind);
        assertThat(option.getVisibility())
                .isEqualTo(visibility);
    }
}
