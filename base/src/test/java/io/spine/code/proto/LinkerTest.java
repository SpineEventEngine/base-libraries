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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests resolving of {@link com.google.protobuf.Descriptors.FileDescriptor FileDescriptor}s.
 */
@DisplayName("Linker should")
class LinkerTest {

    private Linker linker;

    @BeforeEach
    void setUp() throws DescriptorValidationException {
        Collection<FileDescriptorProto> fileSets = FileDescriptors.load();
        linker = new Linker(fileSets);
        linker.resolve();
    }

    @Test
    @DisplayName("resolve files")
    void resolveFiles() {
        var resolved = linker.resolved();
        assertTrue(resolved.size() > 0);
        assertTrue(resolved.containsAll(ImmutableList.of(
                FileName.of("google/protobuf/any.proto"),
                FileName.of("google/protobuf/descriptor.proto")
        )));
    }

    @Test
    @DisplayName("obtain partially resolved files")
    void obtainPartial() {
        // No such in the given test data.
        assertTrue(linker.partiallyResolved()
                         .isEmpty());
    }

    @Test
    @DisplayName("obtain unresolved files")
    void obtainUnresolved() {
        // No such in the given test data.
        assertTrue(linker.unresolved()
                         .isEmpty());
    }

    @Test
    @DisplayName("not leave remaining")
    void doNotLeaveRemaining() {
        assertTrue(linker.remaining()
                         .isEmpty());
    }
}
