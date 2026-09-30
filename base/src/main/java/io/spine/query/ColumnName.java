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

import io.spine.code.proto.FieldDeclaration;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * The name of the column of the record.
 */
public final class ColumnName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private ColumnName(String value) {
        super(value);
    }

    /**
     * Creates a new instance of a column name with the passed value.
     *
     * <p>The passed value must not be empty or blank.
     */
    public static ColumnName of(String value) {
        checkNotEmptyOrBlank(value);
        return new ColumnName(value);
    }

    /**
     * Creates a new instance for the field according to the passed declaration.
     */
    public static ColumnName of(FieldDeclaration field) {
        checkNotNull(field);
        return of(field.name()
                       .value());
    }
}
