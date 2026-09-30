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

package io.spine.format.write

import io.spine.annotation.SPI
import io.spine.format.JacksonSupport
import java.io.File
import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper

/**
 * The abstract base for writers based on the [Jackson](https://github.com/FasterXML) library.
 *
 * If you plan to support a new data format, please see [JacksonSupport].
 *
 * @see JacksonSupport
 * @see io.spine.format.parse.JacksonParser
 */
@SPI
public abstract class JacksonWriter : JacksonSupport(), Writer<Any>

/**
 * Writes JSON files.
 *
 * @see io.spine.format.parse.JsonParser
 * @see ProtoJsonWriter
 */
internal object JsonWriter : JacksonWriter() {

    override fun mapperBuilder(): JsonMapper.Builder =
        JsonMapper.builder()

    override fun write(file: File, value: Any) =
        mapper.writeValue(file, value)
}

/**
 * Writes YAML files.
 *
 * @see io.spine.format.parse.YamlParser
 */
internal object YamlWriter : JacksonWriter(), Writer<Any> {

    override fun mapperBuilder(): YAMLMapper.Builder =
        YAMLMapper.builder()

    override fun write(file: File, value: Any) =
        mapper.writeValue(file, value)
}
