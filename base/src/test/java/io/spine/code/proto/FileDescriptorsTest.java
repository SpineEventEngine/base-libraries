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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("FileDescriptors utility class should")
class FileDescriptorsTest extends UtilityClassTest<FileDescriptors> {

    FileDescriptorsTest() {
        super(FileDescriptors.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FileDescriptor.class, Any.getDescriptor().getFile());
    }

    @Test
    @DisplayName("load main set")
    void loadMainSet() {
        Collection<FileDescriptorProto> fileSets = FileDescriptors.load();
        assertFalse(fileSets.isEmpty());
    }
}
