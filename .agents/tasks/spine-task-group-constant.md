---
slug: spine-task-group-constant
branch: gradle-review-skill
owner: claude
status: in-progress
started: 2026-05-29
related-memories: []
---

## Goal

Replace the bare string literal `"spine"` (the Gradle task group used
by every custom task in this organisation) with a shared constant in
two locations:

1. **In `config`'s `buildSrc/`** — so all build files in `config` and
   all consumer projects that apply `config` reference the same
   symbol instead of repeating the literal.
2. **In `tool-base`** — so the production code of every Spine SDK
   Gradle plugin references the same symbol when it registers or
   configures tasks.

Once both constants exist, `gradle-review` reports a remaining bare
literal `"spine"` as a Nit and recommends the relevant constant as
the replacement.

## Context

- The Spine convention "every custom task has `group = "spine"`" is
  documented in
  [`.agents/skills/gradle-review/spine-task-conventions.md`](../skills/gradle-review/spine-task-conventions.md).
- The `gradle-review` skill (see
  [`../skills/gradle-review/SKILL.md`](../skills/gradle-review/SKILL.md))
  enforces the rule, and lists the constant migration as a Nit until
  the symbol exists.
- Two separate codebases are involved because of dependency direction:
  `buildSrc/` in `config` is on the build classpath of every consumer
  project's `build.gradle.kts`, while `tool-base` is consumed at
  runtime by SDK plugins. A single source-of-truth in `tool-base` and
  a re-export from `buildSrc/` would couple the two — instead each
  side declares its own constant and both keep the same value
  (`"spine"`). The `gradle-review` skill cross-checks both.

## Plan

### A. `config/buildSrc` constant

- [x] Add `object SpineTaskGroup { const val name = "spine" }` in
      `buildSrc/src/main/kotlin/io/spine/gradle/SpineTaskGroup.kt`
      with copyright header and KDoc referencing
      `.agents/skills/gradle-review/spine-task-conventions.md`.
- [x] Migrate every `group = "spine"` usage in `buildSrc/**/*.kt` and
      `buildSrc/**/*.gradle.kts` to the constant. (Verified by
      `rg "group\s*=\s*\"spine\""` — no existing literals in
      `buildSrc/`; the only `"spine"` occurrence there is the
      unrelated artifact-prefix constant in `dependency/local/Base.kt`.)
- [x] Migrate every `group = "spine"` usage in the project's
      `build.gradle.kts` and `settings.gradle.kts` (the constant is
      visible from build files thanks to `buildSrc/`). (Verified — no
      existing literals.)
- [x] Spot-check with `rg -n '"spine"' --type kotlin` (ripgrep's
      built-in `kotlin` type covers both `*.kt` and `*.kts`; the
      short alias `--type kt` is **not** recognised) — only the
      constant declaration and unrelated occurrences (artifact
      prefix in `Base.kt`, exclude rule for `spine-base` in
      `DependencyResolution.kt`) remain.

### B. `tool-base` constant + GitHub issue

- [x] Open the tracking issue under `tool-base` — [tool-base#171][tool-base-171].
- [ ] (Remaining migration is tracked by that issue, not this branch.)

[tool-base-171]: https://github.com/SpineEventEngine/tool-base/issues/171

## Decisions

- **Naming and shape.** `object SpineTaskGroup { const val name = "spine" }`.
  Reference site reads `group = SpineTaskGroup.name`. Mirrors the
  `JsTasks.Group.build` precedent already used inside `buildSrc/` and
  leaves room for related constants later. Consistency with the
  `tool-base` constant — once it exists — is more important than the
  specific shape; the `tool-base` issue should adopt the same shape.
- **Location.** New file at
  `buildSrc/src/main/kotlin/io/spine/gradle/SpineTaskGroup.kt`,
  alongside `TaskName.kt` and other top-level Gradle helpers.
  Visibility is `public` (default) so consumer `build.gradle.kts`
  files can import the symbol.
- **KDoc link form.** Plain text path to
  `.agents/skills/gradle-review/spine-task-conventions.md`; KDoc does
  not resolve relative Markdown links in the IDE, and an absolute
  GitHub URL would couple the source to a specific branch.

## Log

- 2026-05-29 — drafted alongside the `gradle-review` skill, awaiting
  approval to start migration.
- 2026-05-29 — implemented `SpineTaskGroup` in `config/buildSrc`
  (`io.spine.gradle.SpineTaskGroup`). Verified by ripgrep that no
  bare `"spine"` task-group literals exist in `*.kt` or `*.gradle.kts`
  under this repo, so the migration step in section A is a no-op
  inside `config`. The constant is in place for new tasks added here
  and for consumer repositories' build files. The `tool-base`
  constant and its migration remain tracked under
  [tool-base#171][tool-base-171].
