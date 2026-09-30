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
import io.spine.annotation.Internal;
import io.spine.value.ValueHolder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A piece of data stored as a column along with some stored record.
 *
 * <p>Storing the parts of the record in a separate column enabled querying the records
 * by the column values.
 *
 * @param <R>
 *         the type of the stored record
 * @param <V>
 *         the type of the column values
 */
@Immutable
public class RecordColumn<R extends Message, V>
        extends ValueHolder<ColumnName>
        implements Column<R, V> {

    private static final long serialVersionUID = 0L;

    private final Class<V> valueType;
    private final Getter<R, V> getter;

    /**
     * Creates a new column.
     *
     * <p>End-users are responsible for providing the appropriate naming for their columns according
     * to the specification of an underlying storage.
     *
     * <p>The type of the column values is Java-centric. I.e. a responsibility of mapping
     * the column types to the storage-specific data types belongs to a particular
     * storage implementation.
     *
     * @param name
     *         the name of the column; must be non-empty
     * @param valueType
     *         the type of the column values
     * @param getter
     *         the getter returning the value of the column basing on the stored record;
     *         used to compute the value when storing the record
     */
    public RecordColumn(String name, Class<V> valueType, Getter<R, V> getter) {
        this(ColumnName.of(name), valueType, getter);
    }

    /**
     * Creates a new instance of a {@code RecordColumn}.
     *
     * <p>This method is a shortcut for invoking the constructor in those cases, in which
     * the amount of code used is crucial (e.g. in a repetitive declarations of columns).
     *
     * @param name
     *         the name of the column; must be non-empty
     * @param valueType
     *         the type of the column values
     * @param getter
     *         the getter returning the value of the column basing on the stored record;
     *         used to compute the value when storing the record
     * @param <R>
     *         the type of the stored record
     * @param <V>
     *         the type of the column values
     * @return a new instance
     */
    public static <R extends Message, V> RecordColumn<R, V>
    create(String name, Class<V> valueType, Getter<R, V> getter) {
        checkNotNull(name);
        checkNotNull(valueType);
        checkNotNull(getter);
        return new RecordColumn<>(name, valueType, getter);
    }

    /**
     * Creates a new instance.
     *
     * @param name
     *         the name of the column; must be non-empty
     * @param valueType
     *         the type of the column values
     * @param getter
     *         the getter returning the value of the column basing on the stored record;
     *         used to compute the value when storing the record
     */
    public RecordColumn(ColumnName name, Class<V> valueType, Getter<R, V> getter) {
        super(name);
        this.valueType = checkNotNull(valueType, "The type of the returning value must be set.");
        this.getter = checkNotNull(getter, "A getter for the column values must be set.");
    }

    /**
     * Returns the name of the corresponding Protobuf message field.
     */
    @Override
    @Internal
    public ColumnName name() {
        return value();
    }

    /**
     * Returns the type of the column value.
     */
    @Override
    @Internal
    public Class<V> type() {
        return valueType;
    }

    /**
     * Obtains the value of this column for the passed message record.
     *
     * @see io.spine.query.Column.Getter
     */
    @Override
    public @Nullable V valueIn(R record) {
        return getter.apply(record);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecordColumn)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var column = (RecordColumn<?, ?>) o;
        return valueType.equals(column.valueType) &&
                getter.equals(column.getter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valueType, getter);
    }
}
