/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

@file:Suppress("RemoveRedundantQualifierName") // Cannot use imports in some places.

import io.spine.dependency.boms.BomsPlugin
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.repo.standardToSpineSdk
import io.spine.gradle.report.coverage.JacocoConfig
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
    dependencies {
        classpath(io.spine.dependency.local.ToolBase.protobufSetupPlugins)
    }
}

plugins {
    kotlin
    jacoco
    `gradle-doctor`
    `project-report`
    `dokka-for-kotlin`
    `dokka-for-java`
}
apply<BomsPlugin>()

spinePublishing {
    destinations = with(PublishingRepos) {
        setOf(
            cloudArtifactRegistry,
            gitHub("base-libraries")
        )
    }
    modules = productionModuleNames.toSet()
    dokkaJar {
        kotlin = true
        java = true
    }
}

allprojects {
    apply(from = "$rootDir/version.gradle.kts")
    group = "io.spine"
    version = rootProject.extra["versionToPublish"]!!
    repositories.standardToSpineSdk()
    configurations.forceVersions()
}

dependencies {
    dokka(project(":annotations"))
    dokka(project(":base"))
    dokka(project(":format"))
}

configurations.all {
    resolutionStrategy {
        force(
            Base.annotations,
            Base.lib,
            Logging.lib,
        )
    }
}

/**
 * The below block avoids the version conflict with the `spine-base` used
 * by our Dokka plugin and the module of this project.
 *
 * Here's the error:
 *
 * ```
 * Execution failed for task ':dokkaGeneratePublicationHtml'.
 * > Could not resolve all dependencies for configuration ':dokkaHtmlGeneratorRuntimeResolver~internal'.
 *    > Conflict found for the following module:
 *        - io.spine:spine-base between versions 2.0.0-SNAPSHOT.308 and 2.0.0-SNAPSHOT.309
 * ```
 * The problem is not fixed by forcing the version of [Base.lib] in the block above.
 * It requires the code executed on `afterEvaluate`.
 */
afterEvaluate {
    configurations.named("dokkaHtmlGeneratorRuntimeResolver~internal") {
        resolutionStrategy.preferProjectModules()
    }
}

@Suppress("unused")
val dokkaGeneratePublicationHtml by tasks.getting {
    dependsOn(tasks.jar)
}

gradle.projectsEvaluated {
    JacocoConfig.applyTo(project)
    LicenseReporter.mergeAllReports(project)
    PomGenerator.applyTo(project)
}
