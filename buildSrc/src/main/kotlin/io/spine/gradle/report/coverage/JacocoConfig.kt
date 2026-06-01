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

package io.spine.gradle.report.coverage

import io.spine.dependency.test.Jacoco
import io.spine.gradle.SpineTaskGroup
import io.spine.gradle.applyPlugin
import io.spine.gradle.getTask
import io.spine.gradle.report.coverage.TaskName.check
import io.spine.gradle.report.coverage.TaskName.copyReports
import io.spine.gradle.report.coverage.TaskName.jacocoRootReport
import io.spine.gradle.report.coverage.TaskName.jacocoTestReport
import io.spine.gradle.sourceSets
import java.io.File
import java.util.*
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Configures JaCoCo plugin to produce `jacocoRootReport` task which accumulates
 * the test coverage results from all subprojects in a multi-project Gradle build.
 *
 * Users must apply `jacoco` plugin to all the subprojects, for which the report aggregation
 * is required.
 *
 * In a single-module Gradle project, this utility is NOT needed. Just a plain `jacoco` plugin
 * applied to the project is sufficient.
 *
 * Therefore, in case this utility is applied to a single-module Gradle project,
 * an `IllegalStateException` is thrown.
 */
@Deprecated(
    message = "Use `KoverConfig.applyTo(rootProject)`, the Kover-based " +
            "successor that aggregates per-subproject coverage into the " +
            "root `koverXmlReport` and excludes classes compiled from " +
            "`generated/` source directories. " +
            "The `raise-coverage` skill performs this migration automatically. " +
            "See .agents/skills/raise-coverage/references/migrate-to-kover.md.",
    level = DeprecationLevel.WARNING
)
@Suppress("unused", "DEPRECATION")
class JacocoConfig(
    private val rootProject: Project,
    private val reportsDir: File,
    private val projects: Iterable<Project>
) {

    companion object {

        /**
         * A folder under the `buildDir` of the [rootProject] to which the reports will
         * be copied when aggregating the coverage reports.
         *
         * If it does not exist, it will be created.
         */
        private const val reportsDirSuffix = "subreports/jacoco/"

        /**
         * Applies the JaCoCo plugin to the Gradle project.
         *
         * If the passed project has no subprojects, an `IllegalStateException` is thrown,
         * telling that this utility should NOT be used.
         *
         * Registers `jacocoRootReport` task which aggregates all coverage reports
         * from the subprojects.
         */
        fun applyTo(project: Project) {
            project.applyPlugin(BasePlugin::class.java)
            project.gradle.projectsEvaluated {
                val javaProjects: Iterable<Project> = eligibleProjects(project)
                val reportsDir = project.rootProject.layout
                    .buildDirectory.dir(reportsDirSuffix).get().asFile
                JacocoConfig(
                    project.rootProject,
                    reportsDir,
                    javaProjects
                ).configure()
            }
        }

        /**
         * For a multi-module Gradle project, returns those subprojects of the passed [project]
         * which have JaCoCo plugin applied.
         *
         * Throws an exception in case this project has no subprojects.
         */
        private fun eligibleProjects(project: Project): Iterable<Project> {
            val projects: Iterable<Project> =
                if (project.subprojects.isNotEmpty()) {
                    project.subprojects.filter {
                        it.pluginManager.hasPlugin(JacocoPlugin.PLUGIN_EXTENSION_NAME)
                    }
                } else {
                    throw IllegalStateException(
                        "In a single-module Gradle project, `JacocoConfig` is NOT needed." +
                                " Please apply `jacoco` plugin instead."
                    )
                }
            return projects
        }
    }

    private fun configure() {
        configureVersion()
        configureTask()
    }

    private fun configureVersion() {
        val jacoco = rootProject.the<JacocoPluginExtension>()
        jacoco.toolVersion = Jacoco.version
    }

    private fun configureTask() {
        val tasks = rootProject.tasks
        val copyReports = registerCopy(tasks)
        val rootReport = registerRootReport(tasks, copyReports)
        tasks.named(check.name) {
            dependsOn(rootReport)
        }
    }

    private fun registerRootReport(
        tasks: TaskContainer,
        copyReports: TaskProvider<Copy>
    ): TaskProvider<JacocoReport> {
        val allSourceSets = Projects(projects).sourceSets()
        val mainSrcDirs = allSourceSets.mainSrcDirs()
        val humanProducedSourceFolders =
            FileFilter.producedByHuman(mainSrcDirs)

        val filter = CodebaseFilter(
            rootProject,
            mainSrcDirs,
            allSourceSets.mainOutputs()
        )
        val humanProducedCompiledFiles = filter.humanProducedCompiledFiles()

        val rootReport = tasks.register(jacocoRootReport.name, JacocoReport::class.java) {
            group = SpineTaskGroup.name
            description = "Aggregates JaCoCo coverage data from subprojects into a single report"
            dependsOn(copyReports)

            additionalSourceDirs.from(humanProducedSourceFolders)
            sourceDirectories.from(humanProducedSourceFolders)
            executionData.from(rootProject.fileTree(reportsDir))

            classDirectories.from(humanProducedCompiledFiles)
            additionalClassDirs.from(humanProducedCompiledFiles)

            reports {
                html.required.set(true)
                xml.required.set(true)
                csv.required.set(false)
            }
            onlyIf { true }
        }
        return rootReport
    }

    private fun registerCopy(tasks: TaskContainer): TaskProvider<Copy> {
        val everyExecData = mutableListOf<ConfigurableFileCollection>()
        projects.forEach { project ->
            val jacocoTestReport = project.getTask<JacocoReport>(jacocoTestReport.name)
            jacocoTestReport.dependsOn(project.tasks.withType(Test::class.java))
            val executionData = jacocoTestReport.executionData
            everyExecData.add(executionData)
        }

        val originalLocation = rootProject.files(everyExecData)

        val copyReports = tasks.register(copyReports.name, Copy::class.java) {
            group = SpineTaskGroup.name
            description = "Copies JaCoCo `.exec` files from subprojects into root reports folder"
            from(originalLocation)
            into(reportsDir)
            rename {
                "${UUID.randomUUID()}.exec"
            }
            dependsOn(projects.map { it.getTask<JacocoReport>(jacocoTestReport.name) })
        }
        return copyReports
    }
}

/**
 * Extensions for working with groups of Gradle `Project`s.
 */
private class Projects(
    private val projects: Iterable<Project>
) {

    /**
     * Returns all source sets for this group of projects.
     */
    fun sourceSets(): SourceSets {
        val sets = projects.asSequence().map { it.sourceSets }.toList()
        return SourceSets(sets)
    }
}

/**
 * Extensions for working with several of Gradle `SourceSetContainer`s.
 */
private class SourceSets(
    private val sourceSets: Iterable<SourceSetContainer>
) {

    /**
     * Returns the union of Java and Kotlin source folders corresponding to the `main`
     * source set across all underlying [SourceSetContainer]s.
     *
     * Kotlin source directories are registered as a separate [SourceDirectorySet]
     * extension on the source set, not exposed via [allJava][org.gradle.api.tasks.SourceSet.getAllJava].
     * They are surfaced explicitly here so that generated Kotlin code (for example,
     * the output of `protoc-gen-kotlin`) is visible to the coverage filter alongside
     * the Java sources.
     */
    fun mainSrcDirs(): Set<File> {
        return sourceSets
            .asSequence()
            .flatMap { container ->
                val main = container["main"]
                val javaDirs = main.allJava.srcDirs
                val kotlinDirs = (main.extensions.findByName("kotlin") as? SourceDirectorySet)
                    ?.srcDirs
                    ?: emptySet()
                javaDirs + kotlinDirs
            }
            .toSet()
    }

    /**
     * Returns all source set outputs corresponding to the `main` source set type.
     */
    fun mainOutputs(): Set<SourceSetOutput> {
        return sourceSets
            .asSequence()
            .map { it["main"].output }
            .toSet()
    }
}
