# Migrate from vanilla JaCoCo to Kover

Mechanical recipe for the `raise-coverage` skill's Step 0. The skill detects
vanilla JaCoCo in a consumer repo, proposes the migration, waits for approval,
and then applies the edits below. The convention is **Kover Gradle plugin** with
the JaCoCo engine via `useJacoco(version = Jacoco.version)` — JaCoCo-format XML
is preserved, only the Gradle plugin and task names change.

## 1. Purpose

Stand the target repo up on Kover so the rest of the `raise-coverage` skill can
run against a single coverage frontend. After migration, every coverage path
goes through Kover — per-module `koverXmlReport`, root-level `koverXmlReport`
for aggregation, and JaCoCo-format XML at `build/reports/kover/report.xml`.

References:

- Kover Gradle plugin docs: <https://kotlin.github.io/kotlinx-kover/gradle-plugin/>
- Kover migration guide (0.6.x → 0.7+):
  <https://kotlin.github.io/kotlinx-kover/gradle-plugin/migrations/migration-to-0.7.0.html>

## 2. Detection signals

Walk every Gradle module's `build.gradle.kts`. Parse `settings.gradle.kts` for
`include(...)`; honor `project(":x").projectDir = file(...)` overrides.

For each module, grep with the patterns below.

### Vanilla JaCoCo applied

- Plugin block in `plugins { … }`:
  - `^\s*jacoco\b`
  - `id\("jacoco"\)`
- Imperative apply:
  - `apply<JacocoPlugin>\(\)`
  - `apply\(plugin = "jacoco"\)`
- Spine script plugins distributing JaCoCo:
  - `apply\(plugin = "jacoco-`        (covers `jacoco-kotlin-jvm`, `jacoco-kmm-jvm`)
- Spine multi-module aggregation helper:
  - `JacocoConfig\.applyTo`
  - import `io.spine.gradle.report.coverage.JacocoConfig`
- DSL blocks (configuration without explicit plugin id):
  - `jacoco\s*\{`
  - `jacocoTestReport\s*\{`
  - `jacocoTestCoverageVerification\s*\{`
  - `tasks\.named\("jacoco`
- Root-level aggregation:
  - `jacocoRootReport`

### Kover already applied (anywhere on this module)

- Plugin id directly:
  - `org.jetbrains.kotlinx.kover`
- Spine script plugins that auto-apply Kover:
  - `id\("jvm-module"\)` — applies Kover at `jvm-module.gradle.kts:54`
    and configures it at `jvm-module.gradle.kts:99`.
  - `id\("kmp-module"\)` — applies Kover at `kmp-module.gradle.kts:74`
    and configures it at `kmp-module.gradle.kts:181`.
- Spine multi-module Kover aggregation helper (root project only):
  - `KoverConfig\.applyTo`
  - import `io.spine.gradle.report.coverage.KoverConfig`

### Outcome

Classify each module as one of:

| State | Action |
|---|---|
| Kover only | nothing to do |
| Kover + vanilla JaCoCo | strip JaCoCo, keep Kover (decision 4) |
| Vanilla JaCoCo only | migrate to Kover |
| Neither | silent install of Kover (no approval gate) |

If at least one module is "vanilla JaCoCo only" or "Kover + vanilla JaCoCo",
the skill emits the migration proposal and waits.

## 3. Per-module migration

Apply these edits to each module's `build.gradle.kts`:

### Add Kover

Gradle's `plugins { }` block is a constrained DSL that accepts **literal**
plugin IDs and versions only — non-literal constants from `buildSrc` are not
guaranteed to resolve there across the Gradle versions Spine targets. Use
literals; the `Kover` / `Jacoco` constants in `io.spine.dependency.test`
still source-of-truth the values you paste in.

- If the module already applies `jvm-module` or `kmp-module`, **skip this
  step** (log "already via jvm-module" / "already via kmp-module") — both
  script plugins auto-apply Kover.
- If `buildSrc` is on the classpath (the normal Spine consumer case), use the
  bare literal — `buildSrc/build.gradle.kts` pins the Kover plugin version
  globally via the `koverVersion` property, so a per-module version pin is
  redundant:
  ```kotlin
  plugins {
      id("org.jetbrains.kotlinx.kover") // matches `io.spine.dependency.test.Kover.id`
  }
  ```
- Without `buildSrc`, pin the version literally (substitute the current
  `io.spine.dependency.test.Kover.version` value):
  ```kotlin
  plugins {
      id("org.jetbrains.kotlinx.kover") version "0.9.8"
  }
  ```

### Strip JaCoCo

- Remove `jacoco` from `plugins { }` (or the `id("jacoco")` line, or
  `apply<JacocoPlugin>()`, or `apply(plugin = "jacoco")`).
- Replace `apply(plugin = "jacoco-kotlin-jvm")` / `apply(plugin = "jacoco-kmm-jvm")`
  with `id("jvm-module")` / `id("kmp-module")` when that is the module's role;
  otherwise drop and add `id("org.jetbrains.kotlinx.kover")` directly (the
  literal value of `io.spine.dependency.test.Kover.id`; the Gradle Kotlin DSL
  `plugins { }` block does not accept buildSrc constants across the Gradle
  versions Spine supports).
- Rewrite `JacocoConfig.applyTo(rootProject)` (at the root build script) to
  `KoverConfig.applyTo(rootProject)` and update the import to
  `io.spine.gradle.report.coverage.KoverConfig`. The Kover-based helper is the
  documented successor — it wires the Kover plugin at the root, adds
  `kover(project(...))` for every subproject that applies Kover, configures
  `useJacoco(version = Jacoco.version)`, and pushes the generated-class FQNs
  into both the per-module and the root `kover { reports { filters { … } } }`
  blocks. See §4 (root aggregation) for the long-form equivalent if `buildSrc`
  is not on the classpath.
- **Lifecycle gotcha — do not call `KoverConfig.applyTo(...)` from inside
  `gradle.projectsEvaluated { … }`.** Many Spine consumer repos wrap
  `JacocoConfig.applyTo(project)` in that block; carrying the pattern over
  fails with `Cannot run Project.afterEvaluate(Action) when the project is
  already evaluated`, because Kover's plugin registers its own `afterEvaluate`
  hooks at apply time. Lift the call to top level in the root build script.
  `KoverConfig` configures the root eagerly and uses
  `pluginManager.withPlugin(...)` callbacks for subprojects, so modules that
  apply Kover later in the same configuration phase are still discovered
  before Kover finalizes its reports.

### Translation table

| JaCoCo construct | Kover / action |
|---|---|
| `jacoco { toolVersion = Jacoco.version }` | drop (engine version moves to root `useJacoco(...)`) |
| `jacoco { toolVersion = "<other>" }` | **flag** (intentional engine pin — confirm Kover's `useJacoco(version = ...)` matches) |
| `reports { xml=true; html=true; csv=false }` on `jacocoTestReport` | `kover { reports { total { xml { onCheck = true }; html { } } } }` |
| `executionData.setFrom(...)` | **flag** (Kover manages exec data internally) |
| `sourceDirectories.setFrom(...)` | **flag** (Kover infers from compilations) |
| `classDirectories.setFrom(...)` — the Kotlin-JVM/KMP `walkBottomUp` recipe used by `jacoco-kotlin-jvm` / `jacoco-kmm-jvm` | drop; **flag** if the module is non-Kotlin (Kover may not pick up its classes) |
| `reports.xml.outputLocation.set(...)` | **flag** (Kover fixes the path; consumers must follow) |
| `tasks.named("jacocoTestReport") { dependsOn(...) }` | rewrite to `tasks.named("koverXmlReport") { dependsOn(...) }` |
| `violationRules { rule { limit { counter; value; minimum } } }` on `jacocoTestCoverageVerification` | `kover { reports { verify { rule { … } } } }` — counter map below |

### Counter mapping

JaCoCo `counter` → Kover `bound { counter = … }`:

| JaCoCo | Kover |
|---|---|
| `INSTRUCTION` | `INSTRUCTION` |
| `BRANCH` | `BRANCH` |
| `LINE` | `LINE` |
| `METHOD` | `INSTRUCTION` (no direct equivalent) — **flag** |
| `CLASS` | no equivalent — **flag** |

`value` maps directly (`COVEREDRATIO`, `MISSEDRATIO`, `COVEREDCOUNT`,
`MISSEDCOUNT`). `minimum` / `maximum` map directly.

### Simplification with `jvm-module` / `kmp-module`

If the module's role is the standard Spine JVM (or KMP) module, replace the
JaCoCo bits with `id("jvm-module")` (or `id("kmp-module")`). Both script plugins
already apply Kover and configure `useJacoco(...)` plus the XML report — the
migration becomes "remove JaCoCo and let the convention plugin take over".

## 4. Root-level aggregation

Apply at the root only if the source repo had `jacocoRootReport` **or** has more
than one module to aggregate. Skip if the root already applies `jvm-module`
(unusual but possible).

### Preferred — `KoverConfig.applyTo(rootProject)`

When `buildSrc` is on the classpath (the standard Spine setup), use the helper
in `io.spine.gradle.report.coverage.KoverConfig`. It applies Kover at the root,
adds a `kover(project(...))` dependency for every subproject that applies
Kover, configures `useJacoco(version = Jacoco.version)`, and excludes classes
compiled from `generated/` source directories from both per-module and root
reports.

```kotlin
// Root build.gradle.kts
import io.spine.gradle.report.coverage.KoverConfig

KoverConfig.applyTo(rootProject)
```

This is the documented successor to `JacocoConfig.applyTo(rootProject)` and is
what the skill writes when migrating consumer repos.

### Long-form — when `buildSrc` is not available

The `Kover` and `Jacoco` constants live in `buildSrc/.../io/spine/dependency/test/`
and are unreachable when this fallback applies. Paste the literal values
(substitute the current `Kover.version` / `Jacoco.version`):

```kotlin
// Root build.gradle.kts
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

dependencies {
    kover(project(":foo"))
    kover(project(":bar"))
    // … one entry per consuming module
}

kover {
    useJacoco(version = "0.8.14")
    reports {
        total {
            xml { onCheck = true }
            html { }
        }
    }
}
```

Note: the long-form variant does **not** exclude generated code automatically.
Either also apply `KoverConfig.applyTo(rootProject)` (preferred, but requires
`buildSrc`), or push your own exclusion patterns into
`kover { reports { filters { excludes { classes(…) } } } }` at the root and in
each subproject.

If the source repo had a root-level `jacocoTestCoverageVerification`
(`violationRules`), mirror its `rule { limit { … } }` blocks to
`kover { reports { verify { rule { bound { … } } } } }` at the root using the
counter mapping above. Do **not** add root-level rules when the source repo had
none.

## 5. CI, `.codecov.yml`, scripts — substitutions

Apply globally (preserve case in surrounding tokens):

| Old | New |
|---|---|
| `jacocoTestReport` | `koverXmlReport` |
| `jacocoRootReport` | `koverXmlReport` (root) |
| `build/reports/jacoco/test/jacocoTestReport.xml` | `build/reports/kover/report.xml` |
| `build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml` | `build/reports/kover/report.xml` |

### `.github/workflows/*.yml`

Substitute task and path tokens as above. If a step uploads the JaCoCo XML to
Codecov, update the `files:` glob to `**/build/reports/kover/report.xml`.

### `.codecov.yml`

Substitute path tokens as above. Preserve `ignore:` patterns and the
`coverage.status` block verbatim — Codecov only cares about the report path and
the source layout, both of which Kover preserves under `useJacoco(...)`.

### `scripts/*.sh`

Substitute task and path tokens. **Flag** any script that reads raw `.exec`
files (e.g. `build/jacoco/test.exec`) or globs `build/jacoco*` directories —
Kover does not expose them; the script either needs to switch to the XML report
under `build/reports/kover/` or be retired.

## 6. KMP recipe (JVM target only)

Per decision 5, only the JVM target migrates. Non-JVM targets are out of scope.

- Apply `id("org.jetbrains.kotlinx.kover")` (literal; the Gradle Kotlin DSL
  `plugins { }` block does not accept the buildSrc `Kover.id` constant). Or
  use `kmp-module`, which applies Kover automatically.
- Use Kover's default report task and XML:
  - Task: `:<module>:koverXmlReport`
  - XML: `<module>/build/reports/kover/report.xml`

  When the `kover { reports { total { … } } }` block is the only report
  configured (as in `kmp-module.gradle.kts:181-190`), Kover does **not**
  generate a separate `koverXmlReport<Variant>` task per target — the
  `total` report aggregates every Kotlin variant the module declares, and
  because Spine only migrates the JVM target the aggregate is JVM-shaped.
  A `koverXmlReportJvm` task only exists when a named `variant("jvm") { … }`
  block is added explicitly, which `kmp-module` does not do.
- Configuration block at module scope:
  ```kotlin
  kover {
      useJacoco(version = "0.8.14") // matches `io.spine.dependency.test.Jacoco.version`
      reports {
          total {
              xml { onCheck = true }
          }
      }
  }
  ```
  (`kmp-module.gradle.kts:181-190` already has the right shape.)
- CI / `.codecov.yml` use `koverXmlReport` and
  `build/reports/kover/report.xml`, same as for a Kotlin-JVM module.

## 7. Manual-review surfaces

These show up during detection and translation. **Flag** them in the proposal
and ask the user to decide before applying:

- **Custom `sourceDirectories` / `classDirectories`** on `jacocoTestReport` —
  the `walkBottomUp` recipe used by `jacoco-*-jvm.gradle.kts`. Safe to drop for
  standard Kotlin-JVM / KMP layouts; ask if the module is non-Kotlin or has
  unusual source roots.
- **Custom `reports.xml.destination` / `outputLocation`** — Kover writes to a
  fixed path; CI consumers must follow.
- **Custom `executionData` paths** — Kover manages exec data internally; flag
  if anything else (e.g. a coverage uploader) reads them directly.
- **Indirect `jacoco.toolVersion`** — a Gradle property
  (`gradle.properties`, `-PjacocoVersion=…`) or convention plugin pinning a
  non-`Jacoco.version` engine. Decide which version `useJacoco(version = …)`
  should match.
- **Multi-pipeline setups** where both vanilla JaCoCo and Kover are intentional
  (e.g. publishing two different reports for two consumers). Per decision 4 the
  default is to strip JaCoCo, but confirm.
- **`JacocoConfig.applyTo(rootProject)` in a consumer repo** — rewrite to
  `KoverConfig.applyTo(rootProject)` (§3, *Strip JaCoCo*). The Kover helper
  preserves the generated-code exclusion that `JacocoConfig` provided. Do
  **not** simply delete the call — that would silently drop the exclusion and
  cause generated code to appear as uncovered in reports.
- **Custom convention plugins** applying JaCoCo under a name other than
  `jacoco-…` — will be missed by the script-plugin detection in §2. Inspect
  any `buildSrc/src/main/kotlin/*.gradle.kts` that imports `jacoco`.
- **Non-JVM KMP targets** (decision 5 — out of scope). Surface them so the user
  knows their coverage is not migrated.
- **`dependsOn("jacocoTestReport")` from Groovy or external sources** — the
  translation table rewrites Kotlin-script references; Groovy or external
  callers may still reach for the old task name.

## 8. References

- Kover Gradle plugin: <https://kotlin.github.io/kotlinx-kover/gradle-plugin/>
- Kover 0.7 migration guide:
  <https://kotlin.github.io/kotlinx-kover/gradle-plugin/migrations/migration-to-0.7.0.html>
- Kover DSL reference (verify / reports / filters):
  <https://kotlin.github.io/kotlinx-kover/gradle-plugin/dokka/-kover%20-gradle%20-plugin/kotlinx.kover.gradle.plugin.dsl/index.html>
- JaCoCo XML schema (engine, preserved under `useJacoco(...)`):
  <https://www.jacoco.org/jacoco/trunk/coverage/report.dtd>
- Spine convention sources:
  - `buildSrc/src/main/kotlin/jvm-module.gradle.kts` (Kover applied at L54,
    configured at L99)
  - `buildSrc/src/main/kotlin/kmp-module.gradle.kts` (Kover applied at L74,
    configured at L181)
  - `buildSrc/src/main/kotlin/io/spine/dependency/test/Kover.kt`
  - `buildSrc/src/main/kotlin/io/spine/dependency/test/Jacoco.kt`
