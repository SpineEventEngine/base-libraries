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

package io.spine.string;

import com.google.protobuf.Message;
import io.spine.type.Json;

/**
 * The default {@code Stringifier} for {@code Message} classes.
 *
 * <p>Suppose we have the following domain type definitions:
 * <pre>  {@code
 * message TaskId {
 *     string value = 1;
 * }
 *
 * message TaskName {
 *     string value = 1;
 * }
 *
 * message Task {
 *     TaskId id = 1;
 *     TaskName name = 2;
 * }}</pre>
 *
 * <p>The Java code for constructing the {@code Task} instance would be:
 *
 * <pre>  {@code
 * TaskId taskId = TaskId.newBuilder().setValue("task-id").build();
 * TaskName taskName = TaskName.newBuilder().setValue("task-name").build();
 * Task task = Task.newBuilder().setId(taskId).setTaskName(taskName).build();
 * }</pre>
 *
 * <p>Obtaining a default stringifier for the {@code Task} object (which is a {@code Message}),
 * and backward conversion would look like:
 *
 * <pre>  {@code
 * Stringifier<Task> taskStringifier = StringifierRegistry.getStringifier(Task.class);
 *
 * // The result of the below call would be {"id":{"value":"task-id"},"name":{"value":"task-name"}}.
 * String json = taskStringifier.reverse().convert(task);
 *
 * // task.equals(taskFromJson) == true.
 * Task taskFromJson = taskStringifier.convert(json);
 * }</pre>
 *
 * @param <T> the message type
 */
final class DefaultMessageStringifier<T extends Message> extends Stringifier<T> {

    private final Class<T> messageClass;

    DefaultMessageStringifier(Class<T> messageType) {
        super();
        this.messageClass = messageType;
    }

    @Override
    protected String toString(T obj) {
        return Json.toCompactJson(obj);
    }

    @Override
    protected T fromString(String s) {
        return Json.fromJson(messageClass, s);
    }
}
