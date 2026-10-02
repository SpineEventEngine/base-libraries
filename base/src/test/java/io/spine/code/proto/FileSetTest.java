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

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Correspondence;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Empty;
import io.spine.test.code.proto.MessageDecl;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("`FileSet` should")
class FileSetTest {

    private static final Correspondence<FileDescriptor, String> fileNames = Correspondence.from(
            (@NonNull FileDescriptor file, @NonNull String name) -> file.getFullName().equals(name),
            "has name"
    );

    private FileSet fileSet;
    private Path tempDir;

    @BeforeEach
    void load(@TempDir Path tempDir) {
        this.fileSet = FileSet.load();
        this.tempDir = tempDir;
    }

    @Test
    @DisplayName("load mains resources")
    void loadMainResources() {
        assertFalse(fileSet.isEmpty());
    }

    @Test
    @DisplayName("return all declared top-level messages")
    void returnTopLevelMessages() {
        var fileNames =
                ImmutableSet.of(FileName.of("spine/test/code/proto/file_set_test.proto"));
        var set = fileSet.find(fileNames);
        var types = set.topLevelMessages();
        assertThat(types).hasSize(1);

        var onlyElement = types.get(0);
        assertThat(onlyElement.javaClass()).isEqualTo(MessageDecl.class);
    }

    @Test
    @DisplayName("filter message type by predicate")
    void findType() {
        var nameFragment = "Field";
        var types =
                fileSet.findMessageTypes((d) -> d.getName()
                                                 .contains(nameFragment));
        types.forEach(
                type -> assertThat(type.name().value()).contains(nameFragment)
        );
    }

    @Test
    @DisplayName("load from known types")
    void loadFromKnownTypes() throws IOException {
        var file = writeToFile(Empty.getDescriptor()
                                    .getFile()
                                    .toProto());
        var set = FileSet.parseAsKnownFiles(file);
        assertThat(set.files())
                .comparingElementsUsing(fileNames)
                .containsExactly("google/protobuf/empty.proto");
    }

    @Test
    @DisplayName("load from known types and ignore unknown")
    void ignoreUnknown() throws IOException {
        var unknownFile = FileDescriptorProto.newBuilder()
                .setName("example/definition/unknown_file.proto")
                .build();
        var file = writeToFile(unknownFile);
        var set = FileSet.parseAsKnownFiles(file);
        assertThat(set.files())
                .isEmpty();
    }

    private File writeToFile(FileDescriptorProto fileDescriptor) throws IOException {
        var descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(fileDescriptor)
                .build();
        var file = new File(tempDir.toString(), "temp.desc");
        file.createNewFile();
        try (var output = new FileOutputStream(file)) {
            descriptorSet.writeTo(output);
        }
        return file;
    }
}
