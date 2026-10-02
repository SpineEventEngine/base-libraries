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

import com.google.errorprone.annotations.Immutable;
import io.spine.base.EntityState;

import java.io.Serial;

/**
 * A queryable column of an entity that can be passed to the query filters.
 *
 * <p>Normally, instances of this class are provided by the Spine-generated column enumeration and
 * should not be constructed in the user code directly.
 *
 * <p>For each message that represents an entity state and has columns, the Spine routines will
 * generate a nested {@code Column} class that exposes all columns of the entity as
 * {@code EntityColumn} instances. Example:
 *
 * <pre>
 * // Given a message declaration.
 * message ProjectDetails {
 *     option (entity).kind = PROJECTION;
 *
 *     ProjectId id = 1;
 *     ProjectName name = 2 [(column) = true];
 *     int32 task_count = 3 [(column) = true];
 * }
 *
 * // The following Java class will be generated.
 * public final class ProjectDetails ... {
 *
 *     // ...
 *
 *     public static final class Column {
 *
 *         private Column() {
 *             // Prevent instantiation.
 *         }
 *
 *         // Returns the "name" column.
 *        {@literal public static EntityColumn<ProjectDetails, ProjectName> name() {...}}
 *
 *         // Returns the "task_count" column.
 *        {@literal public static EntityColumn<ProjectDetails, Integer> taskCount() {...}}
 *
 *         // Returns all the column definitions for this type.
 *        {@literal public static Set<EntityColumn<ProjectDetails, ?>> definitions()} {
 *            {@literal Set<EntityColumn<ProjectDetails, ?>> result = new HashSet<>();}
 *             result.add(name());
 *             result.add(taskCount());
 *             return result;
 *        }
 *     }
 * }
 * </pre>
 *
 * <p>The values retrieved via {@code static} methods of the {@code Column} type may then be passed
 * to a client to form a query request.
 *
 * <p>See the Spine code generation routines in {@code tool-base} for extensive details on how the
 * types are generated.
 *
 * @param <S> the type of the state of the {@code Entity}
 * @param <V> the type of the column value
 */
@Immutable
public final class EntityColumn<S extends EntityState<?>, V> extends RecordColumn<S, V> {

    @Serial
    private static final long serialVersionUID = 0L;

    public EntityColumn(String columnName, Class<V> valueType, Getter<S, V> getter) {
        super(columnName, valueType, getter);
    }
}
