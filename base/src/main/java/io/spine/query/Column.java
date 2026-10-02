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
import com.google.protobuf.Message;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * A column of a record residing in a storage.
 *
 * <p>Columns are the values of record fields stored along with the record itself. They are used
 * for filtering the results when querying the storage.
 *
 * @param <R>
 *         the type of records
 * @param <V>
 *         the type of column values
 */
public interface Column<R, V> {

    /**
     * The name of the column.
     */
    ColumnName name();

    /**
     * The type of the column value.
     */
    Class<V> type();

    /**
     * Returns the value of the column in a source record.
     */
    @Nullable V valueIn(R source);

    /**
     * A method object serving to obtain the value of the column for some particular record of the
     * matching type.
     *
     * @param <R>
     *         the type of records
     * @param <V>
     *         the type of column values
     */
    @Immutable
    @FunctionalInterface
    interface Getter<R extends Message, V> extends Function<R, V> {
    }
}
