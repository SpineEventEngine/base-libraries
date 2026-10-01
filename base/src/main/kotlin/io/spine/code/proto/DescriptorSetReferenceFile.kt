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
 * A module that contains proto files gets a descriptor set file and the file with the reference
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
     * Creates a reference file pointing to the given descriptor set file.
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
