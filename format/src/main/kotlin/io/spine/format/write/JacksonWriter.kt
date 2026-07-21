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

package io.spine.format.write

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.spine.annotation.SPI
import io.spine.format.JacksonSupport
import java.io.File

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

    override val factory: JsonFactory by lazy {
        JsonFactory()
    }

    override fun write(file: File, value: Any) =
        mapper.writeValue(file, value)
}

/**
 * Writes YAML files.
 *
 * @see io.spine.format.parse.YamlParser
 */
internal object YamlWriter : JacksonWriter(), Writer<Any> {

    override val factory: JsonFactory by lazy {
        YAMLFactory()
    }

    override fun write(file: File, value: Any) =
        mapper.writeValue(file, value)
}
