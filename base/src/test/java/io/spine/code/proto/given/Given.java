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

package io.spine.code.proto.given;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.test.code.proto.FieldContainer;

public final class Given {

    /** Prevents instantiation of this utility class. */
    private Given() {
    }

    public static FieldDescriptor singularField() {
        return fieldWithIndex(0);
    }

    public static FieldDescriptor repeatedField() {
        return fieldWithIndex(1);
    }

    public static FieldDescriptor mapField() {
        return fieldWithIndex(2);
    }

    public static FieldDescriptor primitiveField() {
        return fieldWithIndex(3);
    }

    public static FieldDescriptor messageField() {
        return fieldWithIndex(4);
    }

    public static FieldDescriptor enumField() {
        return fieldWithIndex(5);
    }

    private static FieldDescriptor fieldWithIndex(int index) {
        var fieldContainer = FieldContainer.getDescriptor();
        var fields = fieldContainer.getFields();
        var field = fields.get(index);
        return field;
    }
}
