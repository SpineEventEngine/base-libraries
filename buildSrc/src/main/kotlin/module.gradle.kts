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

import BuildSettings.javaVersion
import io.spine.dependency.boms.BomsPlugin
import io.spine.dependency.build.CheckerFramework
import io.spine.dependency.build.Dokka
import io.spine.dependency.build.ErrorProne
import io.spine.dependency.build.JSpecify
import io.spine.dependency.kotlinx.Coroutines
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Reflect
import io.spine.dependency.test.Jacoco
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.github.pages.updateGitHubPages
import io.spine.gradle.javac.configureErrorProne
import io.spine.gradle.javac.configureJavac
import io.spine.gradle.kotlin.applyJvmToolchain
import io.spine.gradle.kotlin.setFreeCompilerArgs
import io.spine.gradle.report.license.LicenseReporter

plugins {
    `java-library`
    kotlin("jvm")
    id("module-testing")
    id("net.ltgt.errorprone")
    id("pmd-settings")
    id("org.jetbrains.kotlinx.kover")
    id("project-report")
    id("detekt-code-analysis")
    id("dokka-setup")
}
apply<BomsPlugin>()
LicenseReporter.generateReportIn(project)
// Disable `javadoc` task because we use Dokka for building Javadoc-alike docs.
tasks.javadoc.get().enabled = false
CheckStyleConfig.applyTo(project)

project.run {
    configureJava(javaVersion)
    configureKotlin(javaVersion)
    addDependencies()
    forceConfigurations()
    substituteAnnotations()

    val generatedDir = "$projectDir/generated"
    setTaskDependencies(generatedDir)

    configureGitHubPages()
}

typealias Module = Project

fun Module.configureJava(javaVersion: JavaLanguageVersion) {
    java {
        toolchain.languageVersion.set(javaVersion)
    }

    tasks {
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }
}

fun Module.configureKotlin(javaVersion: JavaLanguageVersion) {
    kotlin {
        applyJvmToolchain(javaVersion.asInt())
        explicitApi()
        compilerOptions {
            jvmTarget.set(BuildSettings.jvmTarget)
            setFreeCompilerArgs()
        }
    }

    kover {
        useJacoco(version = Jacoco.version)
        reports.total.xml.onCheck = true
    }
}

/**
 * These dependencies are applied to all subprojects and do not have to
 * be included explicitly.
 *
 * We expose production code dependencies as API because they are used
 * by the framework parts that depend on `base`.
 */
fun Module.addDependencies() = dependencies {
    errorprone(ErrorProne.core)

    compileOnlyApi(CheckerFramework.annotations)
    ErrorProne.annotations.forEach { compileOnlyApi(it) }

    // https://jspecify.dev/docs/using/#gradle
    api(JSpecify.annotations)
}

fun Module.forceConfigurations() {
    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                force(
                    Kotlin.bom,
                    Coroutines.bom,
                    Dokka.BasePlugin.lib,
                    Reflect.lib,
                    // `substituteAnnotations()` replaces it with `:annotations` elsewhere;
                    // this pins the version for the configurations of Dokka.
                    Base.annotations,
                    Base.lib,
                    Logging.lib,
                )
            }
        }
    }
}

/**
 * Resolves the published `spine-annotations` to the `:annotations` module of this build.
 *
 * Gradle knows the module as `io.spine:annotations`, while it is published as
 * `io.spine:spine-annotations`. Because the coordinates differ, conflict resolution
 * does not treat the published artifact, which `spine-logging` and `spine-testlib`
 * bring transitively, as the same module. Without this substitution, both would end up
 * on the classpath, and in the SBOM of the artifact.
 *
 * `:annotations` applies this script too, so its test classpath resolves the published
 * artifact to the module itself, which is intended.
 *
 * The configurations of Dokka resolve its plugins, which are not built here,
 * so they are left as they are.
 */
fun Module.substituteAnnotations() {
    val annotations = Base.annotations.substringBeforeLast(':')
    configurations
        .matching { !it.name.startsWith("dokka") }
        .configureEach {
            resolutionStrategy.dependencySubstitution {
                substitute(module(annotations))
                    .using(project(":annotations"))
                    .because("`:annotations` is the module published as `$annotations`.")
            }
        }
}

fun Module.setTaskDependencies(generatedDir: String) {
    tasks {
        val cleanGenerated = register<Delete>("cleanGenerated") {
            delete(generatedDir)
        }
        clean.configure {
            dependsOn(cleanGenerated)
        }

        project.afterEvaluate {
            val publish = tasks.findByName("publish")
            publish?.dependsOn("${project.path}:updateGitHubPages")
        }
    }
    afterEvaluate {
        configureTaskDependencies()
    }
}

fun Module.configureGitHubPages() {
    updateGitHubPages {
        rootFolder.set(rootDir)
    }
}
