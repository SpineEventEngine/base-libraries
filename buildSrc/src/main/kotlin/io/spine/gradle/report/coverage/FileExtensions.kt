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
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF TE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.gradle.report.coverage

import io.spine.gradle.report.coverage.FileExtension.COMPILED_CLASS
import io.spine.gradle.report.coverage.FileExtension.JAVA_SOURCE
import io.spine.gradle.report.coverage.FileExtension.KOTLIN_SOURCE
import io.spine.gradle.report.coverage.PathMarker.ANONYMOUS_CLASS
import io.spine.gradle.report.coverage.PathMarker.GENERATED
import io.spine.gradle.report.coverage.PathMarker.MAIN_OUTPUT_FOLDER
import java.io.File

/**
 * This file contains extension methods and properties for `java.io.File`.
 */

/**
 * The two-part extension used by `protoc-gen-kotlin` for proto-file-scoped Kotlin
 * helpers (e.g., `FooProtoKt.proto.kt`).
 */
private const val PROTO_KOTLIN_SUFFIX = ".proto.kt"

/**
 * Suffix that the Kotlin compiler appends to the file name when generating the
 * synthetic file class for top-level declarations.
 */
private const val KOTLIN_FILE_CLASS_SUFFIX = "Kt"

/**
 * Parses the name of a class from the absolute path of this file.
 *
 * Treats the fragment between the [precedingMarker] and [extension] as the value to look for.
 * In case the fragment is located and it contains `/` symbols, they are treated
 * as package delimiters and are replaced by `.` symbols before returning the value.
 *
 * If the absolute path of this file has either no [precedingMarker] or no [extension],
 * returns `null`.
 */
internal fun File.parseClassName(
    precedingMarker: PathMarker,
    extension: FileExtension
): String? {
    val index = this.absolutePath.lastIndexOf(precedingMarker.infix)
    return if (index > 0) {
        var inFolder = this.absolutePath.substring(index + precedingMarker.length)
        if (inFolder.endsWith(extension.value)) {
            inFolder = inFolder.substring(0, inFolder.length - extension.length)
            inFolder.replace('/', '.')
        } else {
            null
        }
    } else {
        null
    }
}

/**
 * Attempts to parse the fully-qualified class name from the absolute path of this file,
 * treating it as a path to a compiled `.class` file produced by either `javac` or `kotlinc`.
 *
 * If the `.class` file corresponds to the anonymous or nested class, only the name of the
 * top-level enclosing class is returned.
 */
internal fun File.asJavaCompiledClassName(): String? {
    var className = this.parseClassName(MAIN_OUTPUT_FOLDER, COMPILED_CLASS)
    if (className != null && className.contains(ANONYMOUS_CLASS.infix)) {
        className = className.split(ANONYMOUS_CLASS.infix)[0]
    }
    return className
}

/**
 * Returns the fully-qualified names of compiled JVM classes that originate from this
 * source file, assuming [sourceRoot] is the source-set root under which the file was
 * discovered.
 *
 * The shape of the returned list depends on the source file extension:
 *
 *  - `.java` — a single FQN derived from the path relative to [sourceRoot].
 *  - `.kt` — two FQNs: the declared file/class name, and the same name with `Kt`
 *    appended, which is the synthetic file class that Kotlin emits for top-level
 *    declarations.
 *  - `.proto.kt` — the two-part extension is stripped first; otherwise behaves
 *    like `.kt`. This is the convention used by `protoc-gen-kotlin` for files
 *    holding proto-file-scoped helpers.
 *  - Any other extension — an empty list.
 *
 * Returns an empty list if this file is not located under [sourceRoot].
 */
internal fun File.classNamesIn(sourceRoot: File): List<String> {
    if (!this.startsWith(sourceRoot)) {
        return emptyList()
    }
    val relative = this.toRelativeString(sourceRoot)
    return when {
        relative.endsWith(PROTO_KOTLIN_SUFFIX) -> {
            val base = relative.removeSuffix(PROTO_KOTLIN_SUFFIX).toFqn()
            listOf(base, base + KOTLIN_FILE_CLASS_SUFFIX)
        }
        relative.endsWith(KOTLIN_SOURCE.value) -> {
            val base = relative.removeSuffix(KOTLIN_SOURCE.value).toFqn()
            listOf(base, base + KOTLIN_FILE_CLASS_SUFFIX)
        }
        relative.endsWith(JAVA_SOURCE.value) ->
            listOf(relative.removeSuffix(JAVA_SOURCE.value).toFqn())
        else -> emptyList()
    }
}

private fun String.toFqn(): String = this.replace(File.separatorChar, '.')

/**
 * Tells whether this file is a part of the generated sources, and not produced by a human.
 */
internal val File.isGenerated
    get() = this.absolutePath.contains(GENERATED.infix)
