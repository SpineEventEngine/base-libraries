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

package io.spine.format

import io.spine.annotation.SPI
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.MapperBuilder

/**
 * An abstract base class for I/O operations using the
 * [Jackson](https://github.com/FasterXML) library.
 *
 * The primary subclasses of this class are the abstract types
 * [JacksonWriter][io.spine.format.write.JacksonWriter] and
 * [JacksonParser][io.spine.format.parse.JacksonParser], which provide
 * the foundation for reading from and writing to various data formats.
 *
 * To support a new data format beyond the existing [Format]s,
 * you will typically define a pair of classes extending
 * [JacksonWriter][io.spine.format.write.JacksonWriter] and
 * [JacksonParser][io.spine.format.parse.JacksonParser],
 * rather than subclassing this base class directly.
 *
 * ## Adding Support for a New Format
 *
 * [Format] is a sealed hierarchy, so new formats are added within the `format`
 * module of the Spine SDK. If you are a contributor, follow these steps:
 * 1. Create a new writer class by extending
 *  [JacksonWriter][io.spine.format.write.JacksonWriter].
 * 2. Create a corresponding parser class by extending
 *  [JacksonParser][io.spine.format.parse.JacksonParser].
 * 3. Define an `object` that extends [Format], nesting it under the [Format]
 *  class, and provide instances of the new [writer][Format.writer] and
 *  [parser][Format.parser], along with the appropriate file
 *  [extension(s)][Format.extension]. Nesting helps maintain
 *  an enumeration-like structure. We avoid using a Kotlin `enum` here
 *  because we need generic parameters.
 * 4. Add the new `object` to the [Format.entries] list.
 */
@SPI
public abstract class JacksonSupport {

    /**
     * Creates a builder assembling the [mapper] for a specific data format.
     *
     * Jackson selects the target format by the type of the mapper, such as
     * [JsonMapper][tools.jackson.databind.json.JsonMapper] for JSON or
     * [YAMLMapper][tools.jackson.dataformat.yaml.YAMLMapper] for
     * [YAML](https://github.com/FasterXML/jackson-dataformats-text); other mapper types
     * cover other [text-based](https://github.com/FasterXML/jackson-dataformats-text) or
     * [XML](https://github.com/FasterXML/jackson-dataformat-xml) formats.
     *
     * Subclasses must implement this method by returning a builder appropriate
     * for the target format, e.g., `JsonMapper.builder()`.
     */
    internal abstract fun mapperBuilder(): MapperBuilder<*, *>

    /**
     * A lazily initialized and cached instance of [ObjectMapper] configured for the target format.
     *
     * Upon initialization, the mapper registers the modules discovered via the
     * [ServiceLoader][java.util.ServiceLoader] mechanism, making them available
     * to all [JacksonSupport] subclasses.
     *
     * To contribute a shared [JacksonModule][tools.jackson.databind.JacksonModule],
     * expose it as a `ServiceLoader` service, e.g., by annotating the module class
     * with `@AutoService(JacksonModule::class)`, where `@AutoService` is
     * `com.google.auto.service.AutoService` from Google Auto Service.
     *
     * If a module should only apply to a specific subclass, add it to the builder
     * returned by the [mapperBuilder] of that subclass using [MapperBuilder.addModule].
     *
     * This mapper is configured with [SerializationFeature.INDENT_OUTPUT] enabled.
     * The mapper is immutable: changing other settings requires rebuilding,
     * e.g., `mapper.rebuild()`.
     *
     * Note on `java.time` values: Jackson 3 disables the
     * `DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS` setting by default, so types
     * such as [java.time.Instant] are written as ISO-8601 strings rather than
     * the numeric epoch values Jackson 2 produced. Data written in the old
     * format remains readable.
     *
     * @see MapperBuilder.findAndAddModules
     */
    protected val mapper: ObjectMapper by lazy {
        mapperBuilder()
            .findAndAddModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build()
    }
}
