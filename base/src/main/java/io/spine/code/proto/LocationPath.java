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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.annotation.Internal;
import io.spine.annotation.VisibleForTesting;
import io.spine.type.MessageType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Encapsulates a {@linkplain Location#getPathList() location path}.
 *
 * <p>A location path represents {@linkplain Location#getPathList() list of
 * integers} that used to identify a {@linkplain Location location} in a ".proto" file.
 */
@Immutable
@Internal
public final class LocationPath {

    private final ImmutableList<Integer> path;

    /**
     * Creates a new instance.
     *
     * @param items
     *         the list of path items
     * @param check
     *         if true, the passed iterable will be {@linkplain #checkItem(Integer) checked} for
     *         non-negative values
     */
    private LocationPath(Iterable<Integer> items, boolean check) {
        this.path = ImmutableList.copyOf(check ? checkPath(items) : items);
    }

    /** Creates a copy of the passed path. */
    LocationPath(LocationPath start) {
        this(start.path, false);
    }

    @VisibleForTesting
    LocationPath(Integer... items) {
        this(ImmutableList.copyOf(items), true);
    }

    /**
     * Creates an instance for the passed message descriptor.
     */
    public static LocationPath fromMessage(Descriptor descriptor) {
        ImmutableList.Builder<Integer> path = ImmutableList.builder();
        path.add(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
        if (!MessageType.isTopLevel(descriptor)) {
            Deque<Integer> parentPath = new ArrayDeque<>();
            var containingType = descriptor.getContainingType();
            while (containingType != null) {
                parentPath.addFirst(containingType.getIndex());
                containingType = containingType.getContainingType();
            }
            path.addAll(parentPath);
        }
        path.add(descriptor.getIndex());
        var list = path.build();
        return new LocationPath(list, false);
    }

    /**
     * Appends the path item to the end of this path.
     */
    LocationPath append(Integer... items) {
        var candidates = ImmutableList.copyOf(items);
        checkPath(candidates);
        var combined = toBuilder().addAll(candidates).build();
        return new LocationPath(combined, false);
    }

    private ImmutableList.Builder<Integer> toBuilder() {
        return ImmutableList.<Integer>builder().addAll(this.path);
    }

    @VisibleForTesting
    List<Integer> toList() {
        return path;
    }

    private static ImmutableList<Integer> checkPath(Iterable<Integer> items) {
        items.forEach(LocationPath::checkItem);
        return ImmutableList.copyOf(items);
    }

    private static void checkItem(Integer item) {
        checkArgument(item >= 0);
    }

    /**
     * Converts the instance to the {@code SourceCodeInfo.Location} instance in the given file.
     */
    public SourceCodeInfo.Location toLocationIn(FileDescriptorProto file) {
        var thisPath = toList();
        var locations = file.getSourceCodeInfo().getLocationList();
        for (var location : locations) {
            if (thisPath.equals(location.getPathList())) {
                return location;
            }
        }
        throw newIllegalStateException(
                "The location with the path `%s` is not found in the file \"%s\".",
                this, file.getName()
        );
    }

    @Override
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return (o instanceof LocationPath that)
                && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
