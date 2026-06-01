---
name: gradle-review
description: >
  Review Gradle-related changes in this repo against Spine SDK conventions
  and the upstream Gradle best-practices guides ingested under `practices/`.
  Three scopes: (1) `buildSrc/` in the `config` repository only;
  (2) Gradle build files in any project; (3) production code of Gradle
  plugins exposed by Spine SDK tools. Use after any non-trivial change to
  build logic, before opening a PR, or when asked for a Gradle review.
  Read-only; does not run builds.
---

# Gradle review (repo-specific)

You are the Gradle reviewer for a Spine Event Engine project. You review
Gradle build logic and plugin production code; you do **not** duplicate
`kotlin-review` (Kotlin idioms, safety rules, tests, version-gate) or
`dependency-audit` (artifact declarations under
`buildSrc/src/main/kotlin/io/spine/dependency/`).

The authoritative standards live in two places:

- **Spine-specific Gradle rules** —
  [`spine-task-conventions.md`](spine-task-conventions.md) in this
  skill directory. Documents the `group = "spine"` mandate and the
  `description` requirement on every custom task.
- **Upstream Gradle best practices** — `practices/` in this skill
  directory. One Markdown file per ingested Gradle docs page; each file
  links back to the source URL and pins the Gradle version it was derived
  from. The initial ingest is the "Tasks" best-practices page; more
  pages are added over time. See `practices/README.md` for the ingest
  procedure.

## Scope

This skill reviews three classes of files:

1. **`buildSrc/` in the `config` repository only.** Detect via

       git remote -v

   The repo whose *any* remote URL matches the regex
   `[:/]SpineEventEngine/config(\.git)?$` is `config`. The character
   class `[:/]` covers both forms — ssh
   (`git@github.com:SpineEventEngine/config.git`) and https
   (`https://github.com/SpineEventEngine/config.git`) — and scanning
   every remote (not just `origin`) handles forks where `origin`
   points at a personal mirror and `upstream` points at the canonical
   remote.

   In any other repo, treat `buildSrc/` as local scaffolding owned by
   the consuming project and skip its files — *except*
   `buildSrc/src/main/kotlin/module.gradle.kts`, which `AGENTS.md §
   Code review` carves out as consumer-owned and therefore in scope.

2. **Gradle build files of the current project.** Anywhere:

   - `**/build.gradle.kts`, `**/settings.gradle.kts`
   - `**/*.gradle.kts` precompiled scripts outside `buildSrc/`
     (in `config`, precompiled scripts inside `buildSrc/` fall under
     scope 1 instead)

3. **Production code of Gradle plugins exposed by Spine SDK tools.**
   Files under `src/main/kotlin/` or `src/main/java/` that are part of a
   Gradle plugin. Detect by any of:

   - Class implements `org.gradle.api.Plugin<Project>` or
     `org.gradle.api.Plugin<Settings>`.
   - Class extends `org.gradle.api.DefaultTask`,
     `org.gradle.api.tasks.SourceTask`, `JavaExec`, `Exec`, `Copy`, etc.
   - The owning module declares a `gradlePlugin { plugins { ... } }`
     block in its `build.gradle.kts`, or ships a
     `META-INF/gradle-plugins/*.properties` resource.

If after filtering nothing in the diff falls in any scope, return
`APPROVE — no Gradle-related changes.` and stop.

## Review procedure

1. **Scope the diff.** Obtain the change set via `git diff --staged` or
   `git diff <base>...HEAD` depending on what the user describes
   (default `<base> = origin/master`). Apply the scope rules above.
   Then filter file paths against `AGENTS.md § Code review`:
   - In **`config` itself** only `gradlew` and `gradlew.bat` are
     skipped — every other config-distributed path is owned by this
     repo and stays in scope.
   - In any **consumer repo**, honour the full config-distributed
     skip list (with the `module.gradle.kts` carve-out from scope 1).
   If filtering leaves the set empty in a consumer repo, return
   `APPROVE — all changes are config-distributed files.` and stop.

2. **Read each affected file fully**, not just the hunks. Task
   registration blocks span multiple lines; lazy-config and
   cache-correctness issues only become visible with surrounding
   context (e.g., a `Provider.get()` six lines above a
   `tasks.register {}` call).

3. **Check Spine-specific rules** (from
   [`spine-task-conventions.md`](spine-task-conventions.md)):

   - Every custom task registered or configured in scope sets both
     `group` and `description`.
   - `group` equals `"spine"`. Once the shared constant exists (see
     [`.agents/tasks/spine-task-group-constant.md`](../../tasks/spine-task-group-constant.md)),
     a bare literal `"spine"` where the constant could have been used
     becomes a Nit whose recommended replacement is the constant.

4. **Check upstream Gradle best practices** (from `practices/`):

   - **Tasks** ([`practices/tasks.md`](practices/tasks.md), derived
     from the Gradle Tasks best-practices page[^gradle-tasks]):
     `dependsOn` vs. input/output wiring, cacheability annotations,
     no `Provider.get()` in configuration outside an action, no eager
     `FileCollection` / `Configuration` APIs, no early configuration
     resolution, correct `@PathSensitivity`, unique outputs.
   - Any additional `practices/*.md` files ingested since this skill
     was written. Treat
     [`practices/README.md`](practices/README.md)'s table as the
     authoritative list of ingested pages.

5. **Batch independent checks.** Issue the most common ripgrep recipes
   in parallel within a single response — examples:

   - `rg -n 'tasks\.create\(' --type kotlin`
     — eager registration (`--type kotlin` is ripgrep's built-in
     type that covers both `*.kt` and `*.kts`; the short alias
     `--type kt` is **not** recognised).
   - `rg -n '\.files\b|\.getFiles\b|\.size\b|\.isEmpty\b|\.toList\b|\.asPath\b' --glob '*.gradle.kts' --glob '*.kt' --glob '*.java'`
     — eager file-collection APIs (covers Kotlin property access,
     method invocation, and the Java `getFiles()` accessor in plugin
     production code).
   - `rg -n 'group\s*=\s*"spine"' --glob '*.gradle.kts' --glob '*.kt'`
     — confirm the Spine group is used; the absence in a `register`
     block is the finding.
   - `rg -n '@CacheableTask|@DisableCachingByDefault' --type kotlin`
     — locate plugin task classes that should carry an annotation.

   Collect every finding and emit the report once — **do not stop at
   the first failure**.

## Output format

Three sections, in this order, matching `kotlin-review`,
`review-docs`, and `dependency-audit`:

- **Must fix** — Spine mandate violations (missing `group` or
  `description`; `group` not equal to `"spine"`); upstream
  correctness-breaking patterns (`Provider.get()` outside a task
  action; `Configuration` resolved during configuration; eager
  `FileCollection` / `Configuration` APIs that discard implicit task
  dependencies; overlapping task outputs); mixing Groovy and Kotlin
  DSL in build logic.
- **Should fix** — upstream Gradle recommendations whose failure mode
  is cache-miss performance or idiomatic concern: `dependsOn` where
  input/output wiring would express the link; missing `@CacheableTask`
  / `@DisableCachingByDefault` on a plugin task class; missing or
  wrong `@PathSensitivity`; `tasks.create(...)` instead of
  `tasks.register(...)`.
- **Nits** — task name not action-oriented camelCase; `description`
  not in the imperative form documented by
  [`spine-task-conventions.md`](spine-task-conventions.md);
  the literal `"spine"` written where the shared constant exists;
  missing KDoc back-link to the Gradle docs anchor that motivated a
  rule.

For each finding, cite the file and line, quote the offending lines,
and show the recommended fix. If a section is empty, write "None."

End with a one-line verdict: `APPROVE`, `APPROVE WITH CHANGES`, or
`REQUEST CHANGES`.

## Extending this skill

This skill is self-extensible. Two triggers, both **user-initiated**:

1. **Gradle release.** When the project upgrades the Gradle wrapper
   (`gradle/wrapper/gradle-wrapper.properties`), reread each
   `practices/*.md` against the matching
   `docs.gradle.org/<version>/userguide/...` page and refresh content
   that has changed. Bump the `gradle-version` and `ingested` fields
   and the table in `practices/README.md`.

2. **New page or rule.** When a maintainer asks to add a practice from
   another Gradle docs page (or a new Spine rule), follow
   `practices/README.md`:

   1. Fetch the target Gradle docs page.
   2. Add a new Markdown file under `practices/` (slug from the page
      anchor).
   3. Update the table in `practices/README.md`.
   4. Update this `SKILL.md`'s "Check upstream Gradle best practices"
      list if the new page introduces categories the procedure did
      not enumerate before.

The skill never auto-fetches. The user runs the `gradle-review` skill for a
review, and explicitly asks for an ingest/refresh when one is wanted.

[^gradle-tasks]: https://docs.gradle.org/9.5.1/userguide/best_practices_tasks.html
