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

import com.google.protobuf.gradle.GenerateProtoTask
import io.spine.dependency.build.JSpecify
import io.spine.dependency.lib.AutoService
import io.spine.dependency.lib.AutoServiceKsp
import io.spine.dependency.lib.Guava
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Reflect
import io.spine.dependency.local.TestLib
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.excludeGoogleProtoFromArtifacts

plugins {
    module
    id("com.google.protobuf")
    id("io.spine.descriptor-set-file")
    id("io.spine.generated-sources")
    `project-report`
    ksp
}
apply<IncrementGuard>()

dependencies {
    compileOnly(AutoService.annotations)
    ksp(AutoServiceKsp.processor)

    api(project(":annotations"))
    api(JSpecify.annotations)
    Protobuf.libs.forEach { api(it) }
    api(Guava.lib)

    implementation(Logging.lib)
    implementation(Reflect.lib)
    implementation(Kotlin.reflect)

    /* Have `protobuf` dependency instead of `api` or `implementation` so that proto
       files from the library are included in the compilation. We need this because we
       build our descriptor set files using those standard proto files too.

       See Protobuf Gradle Plugin documentation for details:
           https://github.com/google/protobuf-gradle-plugin#protos-in-dependencies
    */
    protobuf(Protobuf.protoSrcLib)

    testImplementation(TestLib.lib)
    testImplementation(Logging.smokeTest)
    testImplementation(Logging.testLib)?.because("We need `tapConsole`.")
}

configurations.all {
    resolutionStrategy {
        force(
            Base.lib,
            Reflect.lib,
            Logging.lib,
            Logging.libJvm
        )
    }
}

tasks {
    excludeGoogleProtoFromArtifacts()
}


// For generating test fixtures. See `src/test/proto`.
protobuf {
    configurations.excludeProtobufLite()

    protoc {
        artifact = Protobuf.compiler
    }

    generateProtoTasks.all().configureEach {
        // Delete files generated in `com.google` packages because
        // we add `protobuf(Protobuf.protoSrcLib)` dependency above.
        doLast {
            deleteComGoogle("java")
            deleteComGoogle("kotlin")
        }
    }
}

/**
 * Remove the code generated for Google Protobuf library types.
 *
 * The code for the `com.google` package was generated because we wanted
 * to have descriptors for all the types, including those from Google Protobuf library.
 * We want all the descriptors so that they are included into the resources used by
 * the `io.spine.type.KnownTypes` class.
 *
 * Now, as we have the descriptors _and_ duplicating Java or Kotlin code, we delete it to avoid
 * classes that duplicate those coming from Protobuf library JARs.
 *
 * @param language The programming language, either `"java"` or `"kotlin"`.
 */
fun GenerateProtoTask.deleteComGoogle(language: String) {
    val comDirectory = generatedDir(language).resolve("com")
    val googlePackage = comDirectory.resolve("google")

    project.delete(googlePackage)

    // If the `com` directory becomes empty, delete it too.
    if (comDirectory.exists() && comDirectory.isDirectory && comDirectory.list()!!.isEmpty()) {
        project.delete(comDirectory)
    }
}

/**
 * Obtains the path of the `generated` directory under the project root directory.
 */
private val Project.generatedDir
    get() = projectDir.resolve("generated").toPath()

/**
 * Obtains the `generated` directory for the source set of the task.
 *
 * If [language] is specified returns the subdirectory for this language.
 */
private fun GenerateProtoTask.generatedDir(language: String = ""): File {
    val path = "${project.generatedDir}/${sourceSet.name}/$language"
    return File(path)
}

dokka {
    moduleName.set("Base Library")
}
