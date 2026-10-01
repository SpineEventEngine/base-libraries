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

package io.spine.format

import com.google.protobuf.Timestamp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ProtoBinary` format")
internal class ProtoBinarySpec : ProtobufFormatTest(Format.ProtoBinary) {

    /**
     * This test describes the behavior of [Format.ProtoBinary] when
     * another type is attempted to be parsed from a binary source.
     *
     * Unlike [Format.ProtoJson] an attempt to parse with another type
     * leads to creating an empty instance of the requested type with
     * [unknownFields][com.google.protobuf.GeneratedMessage.unknownFields]
     * populated with the data from the parsed bytes.
     *
     * @see io.spine.format.parse.ProtoBinaryParser.doParse
     * @see com.google.protobuf.GeneratedMessageV3.unknownFields
     * @see ProtoJsonSpec
     */
    @Test
    fun `does not force the type but adds unknown fields instead`() {
        write(file, format, instance)
        // We wrote `StringValue`. Now parsing `Timestamp`.
        val timestamp = parse<Timestamp>(file)
        timestamp.seconds shouldBe 0L
        timestamp.nanos shouldBe 0
    }
}
