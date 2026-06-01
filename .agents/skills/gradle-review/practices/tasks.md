---
source: https://docs.gradle.org/9.5.1/userguide/best_practices_tasks.html
gradle-version: 9.5.1
ingested: 2026-05-29
---

# Tasks — Gradle best practices

Source: the Gradle "Best practices for tasks" user-guide page[^src].

The Gradle user guide enumerates a set of best practices for tasks.
Each is mapped below to a Spine review level used by the
`gradle-review` skill.

## Spine-specific must-fix

From [`spine-task-conventions.md`](../spine-task-conventions.md):

- Every custom task must set `group`. The value must equal `"spine"`
  (use the shared constant once introduced — see
  [`.agents/tasks/spine-task-group-constant.md`](../../../tasks/spine-task-group-constant.md)).
- Every custom task must set `description`.

These are **Must fix** findings in `gradle-review`.

## Upstream practices

### 1. Avoid `dependsOn` — *Should fix*

Use input/output wiring (`Provider`-typed `inputs`/`outputs` and
producer-task references) instead of explicit `dependsOn(...)` for the
*action* graph. Wiring tells Gradle *why* one task needs another,
which in turn enables incremental builds and accurate task selection.

`dependsOn` remains correct for lifecycle tasks — tasks without task
actions — per the upstream guidance. (Finalizer relations are wired
with `finalizedBy(...)`, not `dependsOn(...)`.)

### 2. Favor `@CacheableTask` / `@DisableCachingByDefault` — *Should fix*

Annotate task classes for cacheability instead of calling
`outputs.cacheIf {}` at registration time. The annotation documents
the contract in source and avoids re-evaluating the predicate on
every configuration.

### 3. Don't call `get()` on a `Provider` outside a task action — *Must fix*

`Provider.get()` during configuration forces immediate evaluation,
breaks the configuration cache, and serialises work that Gradle would
otherwise run in parallel. Compose providers with `map(...)` /
`flatMap(...)` and defer `get()` to the `@TaskAction` method.

### 4. Group and describe custom tasks — *Must fix*

Set `group` and `description` on every custom task. Tasks without a
group are hidden from `./gradlew tasks` unless `--all` is passed.
They are also excluded from the default IntelliJ IDEA Gradle
tool-window listing (Spine addendum from
[`spine-task-conventions.md`](../spine-task-conventions.md)).

**Spine addendum:** `group` must equal `"spine"`.

### 5. Avoid eager APIs on `FileCollection` / `Configuration` — *Must fix*

`.size()`, `.isEmpty()`, `.files` / `getFiles()`, `asPath()`, and
`.toList()` on a `Configuration` or `FileCollection` trigger
dependency resolution during the configuration phase **and discard
any implicit task dependencies the collection carried** — the latter
is a wrong-outputs failure mode, not a performance one. Consume the
collection lazily via `@InputFiles` / `@Classpath` and
`Provider<...>` chains.

### 6. Don't resolve `Configuration`s before task execution — *Must fix*

Resolving a `Configuration` during configuration (e.g., calling
`configuration.resolve()`, `configuration.resolvedConfiguration`, or
reading `.files` from one) loses task-dependency tracking and slows
unrelated tasks because every build path triggers resolution. Resolve
inside the `@TaskAction` only.

### 7. Use the right `@PathSensitivity` — *Should fix*

Pick the sensitivity that matches what the task's output actually
depends on:

- **`@PathSensitivity.NONE`** — content-only inputs where the file
  name and location do not affect outputs: classpath JAR entries,
  binary blobs, signed/checksummed bundles, etc.
- **`@PathSensitivity.RELATIVE`** — inputs whose relative path is
  part of the task's contract: source-tree files such as `.proto`,
  `.kt`, `.java`, or templated resources, where the relative path
  encodes the package/module/output location.
- **`@PathSensitivity.NAME_ONLY`** — when only the file name (not
  the directory) matters; rare but applicable to per-name lookup
  tables and similar.
- **`@PathSensitivity.ABSOLUTE`** — almost never correct; defeats
  cache portability and should appear with a justifying comment.

Mismatches show up as cache misses (over-strict sensitivity) or
incorrect cache hits (under-strict sensitivity — the more dangerous
direction). Annotating proto-compilation source inputs with `NONE`,
for example, will cause incremental builds to miss renames that
change package structure.

### 8. Use unique output files and directories — *Must fix*

Two tasks must not write to overlapping outputs (either inside one
project or across projects). Overlap causes unnecessary reruns, can
mask stale outputs, and may corrupt incremental builds. Each task
writes to its own deterministic location, typically under
`layout.buildDirectory.dir("…")`.

## Spine additions (not on the upstream page)

- **`tasks.create(...)` vs. `tasks.register(...)` — *Should fix*.**
  `register` is lazy and aligns with every other recommendation on
  this page. New code should always use `register`. Configuring an
  existing task with `tasks.named(...)` is also lazy and preferred
  over `tasks.getByName(...)`.

- **Mixing Groovy and Kotlin DSL — *Must fix*.** Spine projects use
  Kotlin DSL exclusively (`*.gradle.kts`, `*.kt`). Catch any
  `.gradle` Groovy script slipping into `buildSrc/` or the project
  root.

## Nits

- **Task names** should be action-oriented camelCase
  (`generateSpineModel`, not `spine_model_generator` or
  `spineModelGen`).
- **`description`** should read as an imperative sentence
  (`"Generates Spine model classes from .proto definitions"`).
  [`spine-task-conventions.md`](../spine-task-conventions.md) is the
  canonical source; this Nit tracks whatever convention that file
  establishes.
- **`"spine"` as a string literal.** Once the shared constant exists
  (see
  [`.agents/tasks/spine-task-group-constant.md`](../../../tasks/spine-task-group-constant.md)),
  the literal `"spine"` in `buildSrc/` code, build files, or plugin
  production code is a Nit unless wrapped in a comment with a TODO
  referencing the migration.
- **KDoc back-link.** A public custom task class should link the
  Gradle docs anchor that motivated its design (the relevant rule
  above in this file, or the upstream page[^src]) so future readers
  know which best practice the class implements.

[^src]: https://docs.gradle.org/9.5.1/userguide/best_practices_tasks.html
