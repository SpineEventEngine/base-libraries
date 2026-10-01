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

package io.spine.code.proto

import com.google.protobuf.TimestampProto
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.codeGeneratorRequest
import com.google.protobuf.compiler.version
import io.kotest.matchers.shouldBe
import io.spine.string.toBase64Encoded
import io.spine.type.parse
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`CodeGeneratorRequest` parsing should")
internal class CodeGeneratorRequestsSpec {

    private lateinit var requestFile: File
    private lateinit var request: CodeGeneratorRequest

    @BeforeEach
    fun prepareFile(@TempDir dir: File) {
        requestFile = dir.resolve("request.binbp")
        val encodedPath = requestFile.absolutePath.toBase64Encoded()
        request = constructRequest(encodedPath)
    }

    @Test
    fun `parse via 'KClass'`() {
        requestFile.writeBytes(request.toByteArray())
        val input = requestFile.inputStream()
        input.use {
            val parsed = CodeGeneratorRequest::class.parse(it)
            parsed shouldBe request
        }
    }
}

/**
 * Creates a stub instance [CodeGeneratorRequest] initialized with data from [TimestampProto].
 *
 * @param parameterValue
 *         the value to be set to the `parameter` property of the request.
 */
internal fun constructRequest(parameterValue: String): CodeGeneratorRequest =
    codeGeneratorRequest {
        val descr = TimestampProto.getDescriptor()
        protoFile += descr.toProto()
        fileToGenerate += descr.file.name
        compilerVersion = version {
            major = 42
            minor = 314
            patch = 271
        }
        parameter = parameterValue
    }
