---
slug: raise-coverage-kover-migration
branch: coverage-tests-skill
owner: claude
status: in-review
started: 2026-05-30
---

## Goal

Extend the `raise-coverage` skill with a precondition step that migrates a
consumer repo from the vanilla JaCoCo Gradle plugin to JetBrains Kover (with
`useJacoco(version = Jacoco.version)` so the engine and the JaCoCo-format XML
remain unchanged). The skill becomes **Kover-only** post-migration. Adjacent
JaCoCo-distribution code in `config`'s `buildSrc` is marked deprecated with a
pointer to the Kover path; no behaviour change for repos that still consume
the deprecated script plugins.

## Context

The skill in `.agents/skills/raise-coverage/` currently supports two coverage
frontends: Kover (consumer repos) and raw JaCoCo (the `config` repo itself).
Per user decision, the skill collapses to Kover-only and gains a Step 0 that
detects vanilla JaCoCo, proposes a one-shot repo-wide migration, waits for
approval, applies it, smoke-checks, and only then resumes the normal flow.

`config` itself has no production Kotlin/Java code, so the skill never runs
*on* `config`. However, `config` distributes vanilla-JaCoCo infrastructure
(`buildSrc/src/main/kotlin/jacoco-kotlin-jvm.gradle.kts`, `jacoco-kmm-jvm.gradle.kts`,
and the `JacocoConfig` helper) that consumer repos may still apply — these
get deprecation annotations + a runtime `logger.warn` (script plugins only)
but stay on disk so existing consumers keep building. `Jacoco.kt` (engine
version) and `Kover.kt` (plugin version) are unchanged because Kover uses
`useJacoco(version = Jacoco.version)`.

### Decisions (locked — do not re-litigate)

| # | Decision |
|---|----------|
| 1 | Invocation: implicit precondition (Step 0 of the skill) |
| 2 | Scope: repo-wide, proposed once |
| 3 | Trigger: always, unless Kover is already applied everywhere |
| 4 | Both plugins applied: always remove `jacoco`, keep Kover |
| 5 | KMP: JVM-target-only migration via Spine's `kmp-module` script plugin — uses the same `:<module>:koverXmlReport` task and `build/reports/kover/report.xml` path as Kotlin-JVM, because `kmp-module` configures only Kover's `total` report (no named variants, so no `koverXmlReport<Variant>` task is generated) |
| 6 | CI / `.codecov.yml` / scripts: all updated to Kover paths and tasks |
| 7 | Plugin/engine version: reference `io.spine.dependency.test.Kover` / `Jacoco`; do not hardcode |
| 8 | Translation fidelity: best-effort full; flag unmappable constructs |
| 9 | Post-migration: skill flow is Kover-only |
| 10 | No-coverage case: silent install, no approval gate |
| 11 | Verification: smoke check only (`koverXmlReport` exists + parses) |
| 12 | `JacocoConfig` and `jacoco-*.gradle.kts`: mark deprecated, do not delete |

### Verified facts (from Phase 1 exploration)

- `buildSrc/src/main/kotlin/jvm-module.gradle.kts:54` applies Kover; `:99`
  configures it with `useJacoco(version = Jacoco.version)` and XML-on-check.
- `buildSrc/src/main/kotlin/kmp-module.gradle.kts:74` and `:181` mirror the
  above for KMP modules.
- `buildSrc/src/main/kotlin/io/spine/gradle/report/coverage/` contains the
  JaCoCo-pipeline classes to be deprecated: `JacocoConfig`, `TaskName`,
  `CodebaseFilter`, `FileFilter`, `FileExtensions`, `FileExtension`,
  `PathMarker`.
- `scripts/upload-artifacts.sh:38` references `build/jacoco*` directories.
- `.github/workflows/*.yml` in `config` contain no JaCoCo references.

## Implementation

Paths are relative to the `config` repo root.

### Area A — Skill files

#### A1. `.agents/skills/raise-coverage/SKILL.md`

- Frontmatter `description: >`: drop "Kover or JaCoCo frontend"; phrase the
  report as "Kover's JaCoCo-format XML report"; add: "Before anything else,
  ensures the repo is on Kover — if vanilla JaCoCo is detected, proposes a
  one-shot repo-wide migration and waits for approval."
- Body "with JaCoCo" → "with **Kover**'s JaCoCo-format XML report".
- **Scope** bullet rewritten Kover-only. Per-module task
  `:<module>:koverXmlReport`; XML at
  `<module>/build/reports/kover/report.xml`. Same task and path on KMP
  modules configured by Spine's `kmp-module` script plugin — it sets up only
  Kover's `total` report (no named variants, so no
  `koverXmlReport<Variant>` task is generated). Strip every "raw JaCoCo" /
  `jacocoTestReport` / `jacocoRootReport` reference as a normal mode.
- **Insert new `## Step 0 — Ensure Kover`** between `## Inputs` and
  `## Workflow`. Three branches:
  1. Kover applied everywhere → silently proceed.
  2. Nothing applied anywhere → silently install Kover; record
     "Migration: installed Kover" in the final Report; no approval gate.
  3. Vanilla JaCoCo in ≥1 module → emit a proposal and **wait for approval**.
- **Proposal output structure** (Markdown, in this order):
  1. **Detected** — every module applying `jacoco` / `JacocoPlugin` /
     `JacocoConfig.applyTo` / a `jacoco-*.gradle.kts`; annotate "vanilla
     only" vs. "JaCoCo+Kover both"; note any root `jacocoRootReport`.
  2. **Plan** — file edits with paths (per-module `build.gradle.kts`, root
     `build.gradle.kts`, `.codecov.yml`, `.github/workflows/*.yml`,
     `scripts/*.sh`).
  3. **Translation notes** — applicable rows from the migrate-to-kover table.
  4. **Manual-review surfaces** — items the user must decide on.
  5. **Smoke check that will follow** — the E1 commands.
  6. Close with: "Confirm to apply, or call out anything to change first."
- **Wait for approval.** No writes until explicit "go" / "yes" / "apply".
  On adjustment requests, regenerate the proposal and wait again.
- **Apply** per `references/migrate-to-kover.md`; log `edited <path>` per
  file. Any unresolved manual-review surface → stop ("needs your call on
  `<x>`").
- **Smoke check** per E1. Failure → stop; do not fall through.
- **Resume** at Workflow step 1.
- **Workflow step 1** (`--triage`): "per-module `koverXmlReport`, or the
  aggregate `jacocoRootReport` in `config`" → "per-module `koverXmlReport`,
  or the root-level Kover aggregation task `koverXmlReport` if the repo
  wires one".
- **Workflow step 2**: "Detect the coverage frontend and run …" → "Run
  `:<module>:koverXmlReport`" — same task on JVM and KMP modules configured
  by Spine's `kmp-module` script plugin (no named variants → no
  `koverXmlReport<Variant>` task). Drop "either way" from the XML-parsing
  sentence.
- **Workflow step 6**: "(`koverXmlReport` or `jacocoTestReport`)" →
  `:<module>:koverXmlReport`.
- **Report**: add a **Migration** section (emitted only when Step 0 did work).
- **Safety**: add a bullet — "No migration without explicit approval when
  vanilla JaCoCo is detected. Silent install only when *no* frontend is in
  place."

#### A2. `.agents/skills/raise-coverage/references/coverage-signals.md`

- Top blurb: drop the "two frontends" paragraph; replace with a single
  paragraph stating that the engine is JaCoCo and the Spine convention is
  Kover with `useJacoco(version = Jacoco.version)`; the XML is
  JaCoCo-format either way.
- Delete the entire **"Two coverage frontends"** section (current lines
  11–57). Replace with **"Where the report lives"** — per-module task / XML
  path, root-level aggregation paths, `find` recipe if unknown.
- **"Generating a report"**: drop the two JaCoCo `./gradlew` lines; keep
  Kover. Same task on KMP modules configured by Spine's `kmp-module` script
  plugin — no `koverXmlReport<Variant>` task is generated unless a named
  `variant("…") { … }` block is declared.
- **"Extracting gaps for a class"**: drop "or the jacoco path".
- **"KMP / Kotlin-JVM modules"**: keep first sentence; delete the second
  sentence about `jacoco-*-jvm` exec data paths.
- **Verification**: "the **same** report task" → `:<module>:koverXmlReport`.
- **Codecov triage tier appendix**: keep verbatim.

#### A3. `.agents/skills/raise-coverage/references/migrate-to-kover.md` (new)

Eight sections:

1. **Purpose** — one paragraph; link to
   <https://kotlin.github.io/kotlinx-kover/gradle-plugin/> and the
   `migrations/migration-to-0.7.0.html` migration guide.
2. **Detection signals** — grep patterns per module's `build.gradle.kts`:
   - Plugin block: `^\s*jacoco\b` inside `plugins {`, `id\("jacoco"\)`,
     `apply<JacocoPlugin>\(\)`, `apply\(plugin = "jacoco"\)`.
   - Script plugin: `apply\(plugin = "jacoco-`. Covers `jacoco-kotlin-jvm`,
     `jacoco-kmm-jvm`.
   - `JacocoConfig`: `JacocoConfig\.applyTo` or imports of
     `io.spine.gradle.report.coverage.JacocoConfig`.
   - DSL: `jacoco\s*\{`, `jacocoTestReport\s*\{`,
     `jacocoTestCoverageVerification\s*\{`, `tasks\.named\("jacoco`.
   - Kover applied: `org.jetbrains.kotlinx.kover`, or `id("jvm-module")` /
     `id("kmp-module")` (both auto-apply Kover).
   - Root aggregation: `jacocoRootReport`.
   - Multi-module walk: parse `settings.gradle.kts` for `include(...)`;
     honor `project(":x").projectDir = file(...)` overrides.
3. **Per-module migration**:
   - Add Kover via `id(Kover.id)` if `buildSrc` is on the classpath;
     otherwise `id("org.jetbrains.kotlinx.kover") version "<Kover.version>"`.
     If `jvm-module` / `kmp-module` is applied, skip the add (log "already
     via jvm-module").
   - Strip `jacoco` from `plugins { }`.
   - **Translation table**:
     | JaCoCo construct | Kover / action |
     |---|---|
     | `jacoco { toolVersion = Jacoco.version }` | drop (engine version → root `useJacoco(...)`) |
     | `jacoco { toolVersion = <other> }` | **flag** |
     | `reports { xml=true; html=true; csv=false }` | `kover { reports { total { xml { onCheck.set(true) }; html { } } } }` |
     | `executionData.setFrom(...)` | **flag** (Kover-managed) |
     | `sourceDirectories.setFrom(...)` | **flag** (Kover-inferred) |
     | `classDirectories.setFrom(...)` (Kotlin-JVM/KMP `walkBottomUp`) | drop; **flag** if non-Kotlin |
     | `reports.xml.outputLocation.set(...)` | **flag** (fixed path) |
     | `tasks.named("jacocoTestReport") { dependsOn(...) }` | rewrite to `tasks.named("koverXmlReport")` |
     | `violationRules { rule { limit { counter; value; minimum } } }` | `kover { reports { verify { rule { … } } } }`; counter map: INSTRUCTION/BRANCH/LINE = same; METHOD → INSTRUCTION + flag; CLASS → flag |
   - `jvm-module` / `kmp-module` simplification: Kover already there;
     migration becomes "remove JaCoCo bits only".
4. **Root-level aggregation**. Trigger: source had `jacocoRootReport` **or**
   >1 module to aggregate.
   - Apply Kover at root (skip if root applies `jvm-module`).
   - `dependencies { kover(project(":foo")); … }` per consuming module.
   - `kover { useJacoco(version = Jacoco.version); reports { total { xml { onCheck.set(true) }; html { } } } }`.
   - Mirror per-module `violationRules` to root `verify { rule { … } }`
     only if the source repo had a root-level rollup.
5. **CI / `.codecov.yml` / scripts** — substitutions:
   - Workflows: `jacocoTestReport` → `koverXmlReport`; `jacocoRootReport` →
     root `koverXmlReport`;
     `build/reports/jacoco/test/jacocoTestReport.xml` and
     `build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml` →
     `build/reports/kover/report.xml`.
   - `.codecov.yml`: same path tokens; preserve `ignore` and
     `coverage.status` verbatim.
   - `scripts/*.sh`: `build/jacoco*` glob → `build/reports/kover`; **flag**
     scripts reading raw `.exec` paths (e.g.,
     `scripts/upload-artifacts.sh:38` in `config`).
6. **KMP recipe**. JVM-only target. Same task and XML path as Kotlin-JVM:
   `:<module>:koverXmlReport` and `<module>/build/reports/kover/report.xml`.
   Spine's `kmp-module` script plugin configures only Kover's `total` report,
   so no `koverXmlReport<Variant>` task is generated — CI / `.codecov.yml`
   must reference the unsuffixed path. A `koverXmlReportJvm` task would only
   appear if a named `variant("jvm") { … }` block were declared, which
   `kmp-module` does not do.
7. **Manual-review surfaces** (flag and ask):
   - Custom `sourceDirectories` / `classDirectories` on `jacocoTestReport`
     (the `jacoco-*-jvm.gradle.kts` pattern).
   - Custom `reports.xml.destination` / `outputLocation`.
   - Custom `executionData` paths.
   - Indirect `jacoco.toolVersion` (property files, `gradle.properties`).
   - Multi-pipeline setups where both reports are intentional.
   - `JacocoConfig.applyTo(rootProject)` outside `config`.
   - Custom convention plugins applying JaCoCo under a non-`jacoco-…` name.
   - Non-JVM KMP targets — out of scope (decision 5).
8. **References** — links to the Kover migration guide and DSL docs.

#### A4. `.agents/skills/raise-coverage/agents/openai.yaml`

- `short_description`: "Migrate to Kover if needed, then generate unit tests
  to close coverage gaps."
- `default_prompt`: rewrite to name Step 0 — detect setup; if vanilla
  JaCoCo found, propose a one-shot repo-wide migration and wait for
  approval; if nothing applied, install Kover silently. After smoke check,
  run the existing flow (localize from Kover XML; propose; approve;
  generate Kotest / Truth stubs; re-verify). End with: "Tests-only changes
  do not require a version bump."

### Area B — BuildSrc deprecations in `config`

#### B1. `buildSrc/src/main/kotlin/jacoco-kotlin-jvm.gradle.kts` and `jacoco-kmm-jvm.gradle.kts`

Runtime behaviour unchanged. Two edits per file:

1. Below the copyright header, insert a `// DEPRECATED:` block: "This
   script plugin distributes vanilla JaCoCo. New code should apply
   `jvm-module` (or `kmp-module`), which configures Kover via
   `useJacoco(version = Jacoco.version)`. The `raise-coverage` skill
   migrates existing consumers. Kept so older consumer repos continue to
   build; will be removed in a future release."
2. Immediately before `plugins { jacoco }`, add
   `logger.warn("'jacoco-kotlin-jvm' is deprecated; use 'jvm-module' which applies Kover. See .agents/skills/raise-coverage/references/migrate-to-kover.md.")`
   (use `jacoco-kmm-jvm` and `kmp-module` in the KMM file).

#### B2. `@Deprecated` on JaCoCo-pipeline classes under `buildSrc/src/main/kotlin/io/spine/gradle/report/coverage/`

All `DeprecationLevel.WARNING`, no `ReplaceWith` (the replacement is a
multi-block DSL).

- `JacocoConfig.kt` — class `JacocoConfig`: "Use Kover's root-level
  aggregation (`dependencies { kover(project(...)) }` plus
  `kover { useJacoco(version = Jacoco.version); reports { total { xml { onCheck = true } } } }`)
  instead of `JacocoConfig.applyTo(...)`. The `raise-coverage` skill
  performs this migration automatically."
- `TaskName.kt` — enum: "Internal task-name catalog for the deprecated
  `JacocoConfig` pipeline; Kover uses its built-in task names
  (`koverXmlReport`, etc.). Removed when `JacocoConfig` is."
- `CodebaseFilter.kt` — class: "Used only by the deprecated `JacocoConfig`.
  Kover infers source sets and respects `kover { filters { excludes { … } } }`."
- `FileFilter.kt` — object: same wording as `CodebaseFilter`.
- `FileExtensions.kt` — `@file:Deprecated("Path/extension helpers used only by the deprecated `JacocoConfig` pipeline. Removed when `JacocoConfig` is.")`.
- `PathMarker.kt` enum and `FileExtension.kt` enum — same wording as
  `FileExtensions`.

For in-package self-references, accept the warnings or apply
`@Suppress("DEPRECATION")` on the call sites.

#### B3. Untouched

`buildSrc/src/main/kotlin/io/spine/dependency/test/Jacoco.kt` and `Kover.kt`
remain unchanged. `Jacoco.kt` is the engine-version source used by
`kover { useJacoco(version = Jacoco.version) }`.

### Area C — Adjacent docs in `config`

#### C1. `.agents/tasks/raise-coverage.md`

- Decisions table "Coverage engine" row: "exposed via Kover
  (`koverXmlReport`); when a consumer repo still has vanilla JaCoCo, the
  skill migrates it first; Codecov deferred."
- "Verified facts": replace the **JaCoCo paths** bullet with a Kover-paths
  bullet (`<module>/build/reports/kover/report.xml` — same on KMP via
  `kmp-module`, which configures only the `total` report; root aggregation
  `build/reports/kover/report.xml`; Kover manages exec data internally).
- Plan: append `- [ ]` "Kover-only pivot: implement
  `.agents/tasks/raise-coverage-kover-migration.md`".
- Log: append a 2026-05-30 entry summarising the pivot.
- Keep `status: draft`.

#### C2. `.claude/commands/raise-coverage.md`

- Description: "Ensure the repo is on Kover (migrate from JaCoCo if
  needed), then localize coverage gaps and generate missing unit tests for
  a module or path."
- Order bullet: `:<module>:jacocoTestReport` → `:<module>:koverXmlReport`.
  `--triage` line: "ranked JaCoCo gap report" → "ranked Kover gap report".
- After the Skill bullet, add: "First-time setup: the skill enforces
  Kover. If vanilla JaCoCo is found anywhere, the skill proposes a
  repo-wide migration and **waits for your approval**. See
  `references/migrate-to-kover.md`."
- `allowed-tools`: unchanged.

#### C3. Other docs

- `.agents/_TOC.md` — no edit.
- `.agents/tasks/buildsrc-gradle-review-findings.md` — above each item
  referencing `jacoco-kmm-jvm.gradle.kts` or `JacocoConfig.kt` (lines
  77–78, 101–103, 121–122, 169–172, 201–203), insert one line:
  "**Superseded by Kover-only migration**: these files are deprecated; do
  not invest in micro-rewrites. See
  `.agents/skills/raise-coverage/references/migrate-to-kover.md`."
- `scripts/upload-artifacts.sh` — add `# DEPRECATED:` comment line above
  line 38 (`JACOCO_REPORTS=…`) pointing at the migration reference. No
  behaviour change.
- `scripts/buildSrc-migration.kts`, `migrate`, `lychee.toml` — no edits.

### Area E — Verification

#### E1. Step 0 smoke check (post-migration)

1. Run `./gradlew :<module>:koverXmlReport --quiet` on the smallest leaf
   JVM migrated module; if the root was touched, also `./gradlew
   koverXmlReport --quiet`.
2. Assert `<module>/build/reports/kover/report.xml` exists, is non-empty,
   and the first non-XML-declaration line contains `<report `. If
   `DOCTYPE` is present, confirm it points at JaCoCo's `report.dtd`
   (sanity that `useJacoco(...)` took effect).
3. Success = task exits 0 + both assertions pass. Failure = stop with the
   offending file/task.

#### E2. SKILL.md step 6

Addressed in A1 — only the task-name parenthetical changes.

## Plan

- [x] **A1** — restructure `SKILL.md`: Kover-only language; insert Step 0;
  rewrite Workflow steps 1/2/6; add Report Migration section; add Safety
  bullet.
- [x] **A2** — rewrite `references/coverage-signals.md` Kover-only; drop
  the "two frontends" section; keep the Codecov appendix.
- [x] **A3** — create `references/migrate-to-kover.md` (eight sections per
  Implementation Area A3).
- [x] **A4** — update `agents/openai.yaml` `short_description` and
  `default_prompt`.
- [x] **B1** — deprecate `jacoco-kotlin-jvm.gradle.kts` and
  `jacoco-kmm-jvm.gradle.kts` (`// DEPRECATED:` comment + `logger.warn`).
- [x] **B2** — `@Deprecated(WARNING)` on `JacocoConfig`, `TaskName`,
  `CodebaseFilter`, `FileFilter`, `FileExtension`, `PathMarker`, plus
  per-function on `FileExtensions.kt` (file-target `@file:Deprecated` is
  not applicable to the Kotlin `Deprecated` annotation). In-package
  self-references emit warnings by design.
- [x] **C1** — update `.agents/tasks/raise-coverage.md` (Decisions table,
  Verified facts, Plan, Log).
- [x] **C2** — update `.claude/commands/raise-coverage.md` (description,
  Order bullet, First-time setup bullet).
- [x] **C3** — annotate `.agents/tasks/buildsrc-gradle-review-findings.md`
  and `scripts/upload-artifacts.sh` (deprecation pointers; no behaviour
  change).
- [x] **B3** — add `KoverConfig.kt` as the Kover-based successor to
  `JacocoConfig`. Preserves the generated-code exclusion that the JaCoCo
  pipeline provided (consumer requirement: do not include generated code
  as uncovered). `KoverConfig.applyTo(rootProject)` applies Kover at the
  root, adds `kover(project(...))` for every Kover-enabled subproject,
  pins the engine via `useJacoco(version = Jacoco.version)`, and pushes
  the union of generated-class FQNs into both per-module and root
  `kover { reports { filters { excludes { … } } } }`. Updated
  `JacocoConfig`, `CodebaseFilter`, and `FileFilter` deprecation messages
  to point at `KoverConfig`; updated `migrate-to-kover.md` §2 (Kover
  detection signals), §3 (rewrite `JacocoConfig.applyTo` →
  `KoverConfig.applyTo` rather than removing), §4 (KoverConfig is the
  preferred root-level recipe; the long-form block is the fallback when
  `buildSrc` is unavailable), §7 (manual-review surface for the rewrite);
  updated `SKILL.md` Detected list to recognise
  `KoverConfig.applyTo(rootProject)` as a Kover signal.
- [~] **Verify** — partial:
  - [x] `./gradlew -p buildSrc compileKotlin` green (warnings only, intentional).
  - [ ] `markdownlint` — tool not installed locally; defer to CI / reviewer.
  - [ ] Sibling JVM repo dry-run on `base-libraries` — defer (requires
        re-running pilot harness; nothing committed there).
  - [ ] Synthetic vanilla-JaCoCo scratch repo dry-run — defer (scratch
        repo not yet set up).
  - [ ] `jacoco-kotlin-jvm` `logger.warn` surfacing in a tmp consumer —
        defer (same harness).
  - [ ] `./gradlew check -p buildSrc` green in `config` — defer (heavier
        run; compileKotlin proves the deprecation annotations and
        `logger.warn` placement compile cleanly).
- [ ] On merge: flip `status: done` and delete this task file per the
  `.agents/tasks/` lifecycle.

## Risks / notes

- **Detection false negatives.** Custom convention plugins applying
  JaCoCo under a non-`jacoco-…` name will be missed; smoke check may
  still pass because Kover writes its own report. Surfaced as a
  Manual-review item.
- **`dependsOn("jacocoTestReport")` from Groovy or external sources.** The
  translation table rewrites Kotlin-script references; Groovy or external
  usages may be missed. Flagged.
- **Per-module `toolVersion` override.** Always flagged.
- **Non-JVM KMP targets.** Out of scope (decision 5); flagged.
- **`config` itself.** No production code; the skill never runs on
  `config`. Root `build.gradle.kts` does not need Kover.
- **No commits/pushes** unless explicitly authorized.

## Log

- 2026-05-30 — Drafted from clarifying Q&A with the user (12 decisions
  locked). Phase 1 exploration confirmed `jvm-module.gradle.kts:54,99` and
  `kmp-module.gradle.kts:74,181` already wire Kover with
  `useJacoco(version = Jacoco.version)`; the migration targets are the
  vanilla-JaCoCo distribution files (`jacoco-*.gradle.kts`, `JacocoConfig`
  and helpers) plus consumer repos that still use them. Plan approved by
  the user; moved into `.agents/tasks/` as the durable record.
- 2026-05-30 — Implementation complete (A1–C3). SKILL.md, coverage-signals.md
  rewritten Kover-only; migrate-to-kover.md authored; openai.yaml,
  `.claude/commands/raise-coverage.md`, the task file `raise-coverage.md`
  and `buildsrc-gradle-review-findings.md` updated. Both
  `jacoco-*.gradle.kts` script plugins carry the deprecation header and a
  runtime `logger.warn(...)` (placed AFTER `plugins { }` — precompiled
  script plugins require the plugins block to be first). All seven
  classes under `io/spine/gradle/report/coverage/` carry
  `@Deprecated(WARNING)`; `FileExtensions.kt` moved to per-function
  annotations after discovering `@file:Deprecated` isn't supported by the
  Kotlin `Deprecated` annotation. `./gradlew -p buildSrc compileKotlin`
  green — only the intentional in-package deprecation warnings remain.
  Deferred: markdownlint (tool not installed locally), end-to-end pilot
  on `base-libraries`, synthetic vanilla-JaCoCo dry-run, full
  `./gradlew check -p buildSrc`. Nothing committed yet.
- 2026-05-30 — Added `KoverConfig.kt` (B3 above). The user pointed out that
  marking `JacocoConfig` deprecated would silently drop the generated-code
  exclusion that consumer repos depend on. `KoverConfig.applyTo(rootProject)`
  is the live Kover-based successor with the same shape as
  `JacocoConfig.applyTo`: applies Kover at root, wires
  `kover(project(...))` aggregation across all Kover-enabled subprojects,
  pins `useJacoco(version = Jacoco.version)`, and pushes per-module +
  union root excludes for classes compiled from `*/generated/*` source
  directories. FQN derivation reuses the algorithm from
  `CodebaseFilter`/`FileExtensions` (`.java` → one FQN; `.kt` / `.proto.kt`
  → declared class + `<Name>Kt` synthetic) and emits both `FQN` and
  `FQN$*` exclusion patterns to cover nested classes. The skill's
  migration step now rewrites `JacocoConfig.applyTo` → `KoverConfig.applyTo`
  instead of deleting the call. `./gradlew -p buildSrc compileKotlin`
  green.
