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

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.DescriptorProtos.FileDescriptorSet.parseFrom
import com.google.protobuf.InvalidProtocolBufferException
import io.spine.type.ExtensionRegistryHolder.extensionRegistry
import io.spine.util.Exceptions.illegalArgumentWithCauseOf
import java.io.IOException
import java.io.InputStream
import java.util.Optional

/**
 * Static factory methods for creating instances of [FileDescriptorSet]
 * that wrap handling of checked [InvalidProtocolBufferException].
 *
 * If an error occurs, the methods throw [IllegalArgumentException] with the checked exception
 * as the cause, or return empty [Optional].
 */
public object FileDescriptorSetReader {

    /** Parses a descriptor set from the given byte array. */
    @JvmStatic
    public fun parse(bytes: ByteArray): FileDescriptorSet = try {
        parseFrom(bytes, extensionRegistry)
    } catch (e: InvalidProtocolBufferException) {
        throw illegalArgumentWithCauseOf(e)
    }

    /** Attempts to parse a descriptor set from the given byte array. */
    @JvmStatic
    public fun tryParse(bytes: ByteArray): Optional<FileDescriptorSet> = try {
        val result = parseFrom(bytes, extensionRegistry)
        Optional.of(result)
    } catch (ignored: InvalidProtocolBufferException) {
        Optional.empty()
    }

    /** Parses a descriptor set from the given stream. */
    @JvmStatic
    public fun parse(stream: InputStream): FileDescriptorSet = try {
        parseFrom(stream, extensionRegistry)
    } catch (e: IOException) {
        throw illegalArgumentWithCauseOf(e)
    }
}
