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

@file:Suppress("RemoveRedundantQualifierName") // Cannot use imports in some places.

import io.spine.dependency.boms.BomsPlugin
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.repo.standardToSpineSdk
import io.spine.gradle.report.coverage.KoverConfig
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
    `gradle-doctor`
    `project-report`
    `dokka-setup`
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

tasks.named("dokkaGeneratePublicationHtml") {
    dependsOn(tasks.jar)
}

KoverConfig.applyTo(project)

gradle.projectsEvaluated {
    LicenseReporter.mergeAllReports(project)
    PomGenerator.applyTo(project)
}
