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

import com.google.protobuf.Message;
import io.spine.annotation.SPI;

/**
 * A parameter of a {@link RecordQuery}.
 *
 * @param <R>
 *         the type of the queried record
 * @param <V>
 *         the type of the record field value to use in querying
 */
@SPI
public final class RecordSubjectParameter<R extends Message, V>
        extends SubjectParameter<R, RecordColumn<R, V>, V> {

    public RecordSubjectParameter(RecordColumn<R, V> column, ComparisonOperator operator, V value) {
        super(column, operator, value);
    }
}
