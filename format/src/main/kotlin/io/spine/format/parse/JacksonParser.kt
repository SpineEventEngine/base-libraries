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

package io.spine.format.parse

import com.google.common.io.ByteSource
import io.spine.annotation.SPI
import io.spine.format.JacksonSupport
import io.spine.format.write.JsonWriter
import io.spine.format.write.YamlWriter
import java.nio.charset.Charset.defaultCharset
import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper

/**
 * The abstract base parsers of text-based formats backed by
 * the [Jackson](https://github.com/FasterXML) library.
 *
 * If you plan to support a new data format, please see [JacksonSupport].
 *
 * @see JacksonSupport
 * @see io.spine.format.write.JacksonWriter
 */
@SPI
public abstract class JacksonParser : JacksonSupport(), Parser<Any> {

    final override fun <T : Any> parse(source: ByteSource, cls: Class<out T>): T {
        val charSource = source.asCharSource(defaultCharset())
        return charSource.openBufferedStream().use {
            mapper.readValue(it, cls)
        }
    }
}

/**
 * The parser for JSON.
 *
 * @see io.spine.format.write.JsonWriter
 */
internal data object JsonParser : JacksonParser() {

    override fun mapperBuilder(): JsonMapper.Builder =
        JsonWriter.mapperBuilder()
}

/**
 * The parser for YAML.
 *
 * @see io.spine.format.write.YamlWriter
 */
internal data object YamlParser : JacksonParser() {

    override fun mapperBuilder(): YAMLMapper.Builder =
        YamlWriter.mapperBuilder()
}
