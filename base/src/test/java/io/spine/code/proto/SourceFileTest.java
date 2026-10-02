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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.test.compiler.message.Top;
import io.spine.type.MessageType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`SourceFile` should")
class SourceFileTest {

    private static final FileDescriptor TEST_FILE_DESCRIPTOR = Top.getDescriptor()
                                                                  .getFile();
    private SourceFile sourceFile;

    @BeforeEach
    void setUp() {
        sourceFile = SourceFile.from(TEST_FILE_DESCRIPTOR);
    }

    @Test
    @DisplayName("search nested declarations recursively")
    void search_nested_declarations_recursively() {
        var nestedForNested = Top.NestedForTop.NestedForNested.getDescriptor();
        var expectedTypeName = nestedForNested.getFullName();
        var simpleTypeName = nestedForNested.getName();
        var result = findDeclaration(simpleTypeName);
        assertEquals(expectedTypeName, result.name()
                                             .value());
    }

    private MessageType findDeclaration(String name) {
        Predicate<DescriptorProto> predicate = new MessageWithName(name);
        Collection<MessageType> searchResult = sourceFile.allThat(predicate);
        assertEquals(searchResult.size(), 1);
        return searchResult.iterator()
                           .next();
    }

    /**
     * Test predicate that matches a message declaration by its name.
     */
    private static class MessageWithName implements Predicate<DescriptorProto> {

        private final String name;

        private MessageWithName(String name) {
            this.name = name;
        }

        @Override
        public boolean test(@Nullable DescriptorProto input) {
            checkNotNull(input);
            var messageName = input.getName();
            return messageName.equals(name);
        }
    }
}
