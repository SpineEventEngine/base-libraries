/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
