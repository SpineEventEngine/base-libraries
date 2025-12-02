/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.code.proto

import io.spine.annotation.VisibleForTesting
import io.spine.io.Resource
import io.spine.util.Exceptions.illegalStateWithCauseOf
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * A descriptor set reference file ([desc.ref][NAME]) contains one or more references
 * to descriptor set files that are created by `GenerateProtoTask` files of
 * Protobuf Gradle Plugin applied to a project.
 *
 * The references are file names of the resources packed along with the [desc.ref][NAME] file.
 * The [desc.ref][NAME] file is needed to avoid walking through the whole classpath for
 * finding descriptor set files.
 *
 * A module which contains proto files gets a descriptor set file and the file with the reference
 * to it when the Spine's Descriptor Set File Gradle Plugin is applied to the project.
 *
 * The plugin can be applied either directly or indirectly e.g.,
 * via Spine Compiler Gradle Plugin, or CoreJvm Gradle Plugin.
 */
public object DescriptorSetReferenceFile {

    /**
     * The class loader used to load resources.
     */
    private val classLoader: ClassLoader by lazy {
        Thread.currentThread().contextClassLoader
    }

    /**
     * A name of the file that contains references to a number of Protobuf descriptor set files.
     *
     * There may be multiple such files present in one project.
     * The file is created by Gradle plugins that instruct Protobuf Gradle Plugin
     * to create descriptor set files. This file gathers references to all such files that
     * come from dependencies of the module.
     */
    public const val NAME: String = "desc.ref"

    private val resourceFile: Resource by lazy {
        Resource.file(NAME, classLoader)
    }

    /**
     * Create a reference file pointing to the given descriptor set file.
     *
     * If the reference file already exists, it will be overwritten.
     *
     * @param dir The directory to place the file.
     *        If the directory does not exist, it will be automatically created.
     * @param target The descriptor set file to reference.
     */
    @JvmStatic
    public fun create(dir: File, target: File) {
        val result = File(dir, NAME)
        result.parentFile.mkdirs()
        result.writeText(target.name)
    }

    /**
     * Loads all descriptor set reference files found in classpath resources.
     *
     * Searches for all [desc.ref][NAME] files in classpath resources,
     * reads their contents and returns a list of [Resource]s corresponding to
     * the descriptor set files referenced in them.
     *
     * Each returned resource corresponds to a unique descriptor set file.
     * Duplicate entries are filtered out.
     *
     * @return list of resources pointing to descriptor set files.
     */
    @JvmStatic
    public fun loadAll(): List<Resource> {
        val allDescRefFiles = resourceFile.locateAll()
        return loadFromResources(allDescRefFiles)
    }

    @VisibleForTesting
    internal fun loadFromResources(resources: Collection<URL>): List<Resource> =
        resources.map { readFile(it) }
            .flatMap { it.lines().filter { line -> filterLine(line) } }
            .distinct()
            .map { Resource.file(it, classLoader) }

    /**
     * Accepts descriptor reference file lines that are not empty.
     */
    private fun filterLine(line: String): Boolean = line.isNotBlank()
}

private fun readFile(resource: URL): String = try {
    String(resource.readBytes(), StandardCharsets.UTF_8)
} catch (e: IOException) {
    throw illegalStateWithCauseOf(e)
}
