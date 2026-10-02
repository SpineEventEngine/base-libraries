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

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import io.spine.type.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.code.proto.CodeGeneratorRequestParsingSpecKt.constructRequest;
import static io.spine.string.Strings.toBase64Encoded;
import static kotlin.io.FilesKt.writeBytes;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("`CodeGeneratorRequest` Java API should")
class CodeGeneratorRequestsJavaSpec {

    private File requestFile;
    private CodeGeneratorRequest request;

    @BeforeEach
    void prepareFile(@TempDir Path dir) {
        requestFile = dir.resolve("request.binbp").toFile();
        var encodedPath = toBase64Encoded(requestFile.getAbsolutePath());
        request = constructRequest(encodedPath);
    }

    @Test
    @DisplayName("provide parsing from an input stream")
    void parsing() {
        writeBytes(requestFile, request.toByteArray());
        try (var input = new FileInputStream(requestFile)) {
            var parsed = Binary.parse(CodeGeneratorRequest.class, input);
            assertThat(parsed).isEqualTo(request);
        } catch (IOException e) {
            fail("Failed to parse the request file.", e);
        }
    }
}
