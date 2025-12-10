/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.annotation.VisibleForTesting;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.Descriptors.FileDescriptor.buildFrom;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Builds a set of {@link FileDescriptor}s from a list of {@link FileDescriptorProto}.
 */
final class Linker {

    private static final FileDescriptor[] NO_DEPENDENCIES = {};

    private final List<FileDescriptorProto> input;

    private final List<FileDescriptorProto> remaining;

    private final FileSet resolved;
    private final FileSet partiallyResolved;
    private final FileSet unresolved;

    Linker(Iterable<FileDescriptorProto> input) {
        this.input = ImmutableList.copyOf(input);
        this.remaining = Lists.newArrayList(input);
        this.resolved = FileSet.newInstance();
        this.partiallyResolved = FileSet.newInstance();
        this.unresolved = FileSet.newInstance();
    }

    static FileSet link(Collection<FileDescriptorProto> files) {
        var linker = new Linker(files);
        try {
            linker.resolve();
        } catch (DescriptorValidationException e) {
            throw newIllegalStateException(e, "Unable to link descriptor set files.");
        }
        var result = linker.resolved()
                           .union(linker.partiallyResolved())
                           .union(linker.unresolved());
        return result;
    }

    void resolve() throws DescriptorValidationException {
        // Make sure this method is called only after the constructor once.
        checkState(input.size() == remaining.size());
        findNoDependencies();
        findResolved();
        findPartiallyResolved();
        addUnresolved();
    }

    private void findNoDependencies() throws DescriptorValidationException {
        var iterator = remaining.iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next.getDependencyCount() == 0) {
                var fd = buildFrom(next, NO_DEPENDENCIES, true);
                resolved.add(fd);
                iterator.remove();
            }
        }
    }

    /**
     * Iterates over the remaining files while the list is not empty,
     * or no more resolved files found.
     */
    private void findResolved() throws DescriptorValidationException {
        var resolvedFound = true;
        while (!remaining.isEmpty() && resolvedFound) {
            resolvedFound = doFindResolved();
        }
    }

    private boolean doFindResolved() throws DescriptorValidationException {
        var result = false;
        var iterator = remaining.iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            var dependencyList = dependencies(next);
            if (resolved.containsAll(dependencyList)) {
                var dependencies = resolved.find(dependencyList);
                var newResolved = buildFrom(next, dependencies.toArray(), true);
                resolved.add(newResolved);
                result = true;
                iterator.remove();
            }
        }
        return result;
    }

    private void findPartiallyResolved() throws DescriptorValidationException {
        var partiallyResolvedFound = true;
        while (!remaining.isEmpty() && partiallyResolvedFound) {
            partiallyResolvedFound = doFindPartiallyResolved();
        }
    }

    private boolean doFindPartiallyResolved() throws DescriptorValidationException {
        var result = false;
        var partialAndResolved = resolved.union(partiallyResolved);
        var iterator = remaining.iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            var dependencyList = dependencies(next);
            var dependencies = partialAndResolved.find(dependencyList);
            if (dependencies.isEmpty()) {
                var newPartial = buildFrom(next, dependencies.toArray(), true);
                partiallyResolved.add(newPartial);
                partialAndResolved.add(newPartial);
                result = true;
            }
            iterator.remove();
        }
        return result;
    }

    /**
     * Adds unresolved descriptors.
     *
     * <p>Even though unresolved by now descriptors can be resolvable to each other isolation,
     * we would not be able to use that information for code generation. That's why this method
     * simply adds the remaining files as unresolvable without attempting to resolve them within
     * the group.
     */
    private void addUnresolved() throws DescriptorValidationException {
        while (!remaining.isEmpty()) {
            var first = remaining.get(0);
            var fd = buildFrom(first, NO_DEPENDENCIES, true);
            unresolved.add(fd);
            remaining.remove(first);
        }
    }

    private static Collection<FileName> dependencies(FileDescriptorProto file) {
        return file.getDependencyList()
                   .stream()
                   .map(FileName::of)
                   .collect(toList());
    }

    @VisibleForTesting
    List<FileDescriptorProto> remaining() {
        return ImmutableList.copyOf(remaining);
    }

    FileSet resolved() {
        return resolved;
    }

    FileSet partiallyResolved() {
        return partiallyResolved;
    }

    FileSet unresolved() {
        return unresolved;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("input", namesForDisplay(input))
                          .add("remaining", namesForDisplay(remaining))
                          .add("resolved", resolved)
                          .add("partiallyResolved", partiallyResolved)
                          .add("unresolved", unresolved)
                          .toString();
    }

    private static String namesForDisplay(Collection<FileDescriptorProto> descriptors) {
        return descriptors.stream()
                          .map(FileDescriptorProto::getName)
                          .sorted()
                          .collect(joining(lineSeparator()));
    }
}
