/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Message;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A list of {@link io.spine.query.RecordColumn RecordColumn}s.
 *
 * <p>The instances of this type are immutable.
 *
 * @param <R>
 *         the type of records that columns are described
 * @implNote This type delegates all of its operations to a nested {@link ImmutableList}.
 */
@Immutable
@SuppressWarnings("DuplicateStringLiteralInspection") /* Same message in the annotation. */
public final class Columns<R extends Message> implements List<RecordColumn<R, ?>> {

    private final ImmutableList<RecordColumn<R, ?>> delegate;

    private Columns(ImmutableList<RecordColumn<R, ?>> delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new instance of {@code Columns} from the passed {@code RecordColumn}s.
     *
     * @param columns
     *         the columns to join in a list
     * @param <R>
     *         the type of records that columns are joined into a list
     * @return a new instance of the column list
     */
    @SafeVarargs
    public static <R extends Message> Columns<R> of(RecordColumn<R, ?>... columns) {
        checkNotNull(columns);
        var asList = ImmutableList.copyOf(columns);
        var result = new Columns<>(asList);
        return result;
    }

    @Override
    public RecordColumn<R, ?> get(int index) {
        return delegate.get(index);
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public Spliterator<RecordColumn<R, ?>> spliterator() {
        return delegate.spliterator();
    }

    @NonNull
    @Override
    public UnmodifiableIterator<RecordColumn<R, ?>> iterator() {
        return delegate.iterator();
    }

    @Override
    public void forEach(Consumer<? super RecordColumn<R, ?>> action) {
        delegate.forEach(action);
    }

    @Override
    public Stream<RecordColumn<R, ?>> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<RecordColumn<R, ?>> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @NonNull
    @Override
    public ListIterator<RecordColumn<R, ?>> listIterator() {
        return delegate.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<RecordColumn<R, ?>> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @NonNull
    @Override
    public List<RecordColumn<R, ?>> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @NonNull
    @Override
    @SuppressWarnings("SuspiciousToArrayCall")  /* This is a responsibility of a caller. */
    public <T> T[] toArray(@NonNull T[] array) {
        return delegate.toArray(array);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public void replaceAll(UnaryOperator<RecordColumn<R, ?>> operator) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public void sort(Comparator<? super RecordColumn<R, ?>> c) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean removeIf(Predicate<? super RecordColumn<R, ?>> filter) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean add(RecordColumn<R, ?> column) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean addAll(@NonNull Collection<? extends RecordColumn<R, ?>> c) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean addAll(int index, @NonNull Collection<? extends RecordColumn<R, ?>> c) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public void add(int index, RecordColumn<R, ?> element) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public RecordColumn<R, ?> set(int index, RecordColumn<R, ?> element) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean remove(Object o) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean removeAll(Collection<?> c) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public boolean retainAll(Collection<?> c) {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public void clear() {
        throw unsupported();
    }

    /**
     * Always throws an {@link UnsupportedOperationException}.
     */
    @Override
    @DoNotCall("Always throws `UnsupportedOperationException`")
    public RecordColumn<R, ?> remove(int index) {
        throw unsupported();
    }

    private static RuntimeException unsupported() {
        throw new UnsupportedOperationException("`Columns` is immutable.");
    }
}
