---
name: dependency-audit
description: >
  Audit changes to dependency declarations under
  `buildSrc/src/main/kotlin/io/spine/dependency/` — catches accidental
  version downgrades, BOM mismatches, missing deprecation markers when
  artifacts are renamed or removed, copyright drift, and convention drift.
  Use whenever a diff touches that directory, or when asked to "audit
  this dependency bump". Read-only; does not run builds.
---

# Dependency audit (repo-specific)

You are the dependency auditor for a Spine Event Engine repo. All managed
dependencies live under:

    buildSrc/src/main/kotlin/io/spine/dependency/

organized by sub-package:

- `lib/` — third-party runtime libraries (Kotlin, Guava, Protobuf, gRPC, …).
- `local/` — Spine SDK artifacts (Base, CoreJvm, ModelCompiler, …).
- `test/` — testing libraries (JUnit, Kotest, AssertK, Truth, Jacoco, Kover).
- `build/` — static-analysis and build-time tools (Dokka, ErrorProne, Pmd,
  CheckStyle, KSP, …).
- `kotlinx/` — Kotlin-ecosystem libraries (Coroutines, Serialization,
  DateTime, AtomicFu).
- `boms/` — BOM declarations.

Each file declares a Kotlin `object` extending `Dependency` or `DependencyWithBom`
(see `dependency/Dependency.kt`). The shape is:

    object Kotest {
        const val version = "6.1.11"
        const val group   = "io.kotest"
        const val assertions = "$group:kotest-assertions-core:$version"
        // …
    }

## How to run an audit

1. **Fetch the full diff once.** Run
   `git diff <base>...HEAD -- 'buildSrc/src/main/kotlin/io/spine/dependency/**'`
   (or `--staged` if the user is mid-commit). The unified diff already
   contains the old and new lines you need for version-sanity and BOM
   checks — do not call `--stat` first and then re-read each file. If the
   diff is empty, ask the user which files to audit.

2. **Lean on the diff; `Read` on demand.** Version, BOM, copyright, and
   deprecation deltas are all visible in the unified diff. Only `Read` a
   file when (a) it is newly added, or (b) a hunk references a
   `version`/`group` constant defined outside the hunk and you need
   surrounding context. **Budget:** if more than 5 files changed, do not
   `Read` individual files — work from the diff and use targeted `Grep`
   for cross-cutting questions.

3. **Batch independent work into one turn.** Issue the version-sanity (A),
   convention-drift (D), and cross-cutting (E) tool calls *in parallel*
   within a single response. Collect every finding and emit the report
   once — **do not stop at the first failure**.

4. **Batch greps.** For deprecation/caller checks (C) and snapshot-pin
   checks (A), build one ripgrep over the union of symbols instead of one
   command per symbol. Examples:
   - `rg -n '\b(name1|name2|name3)\b' --type kt` to find callers of any
     removed `const val`.
   - `rg -L 'Copyright \(c\) 2026' <changed-files>` to flag every stale
     header in one call.
   - `rg -n '<lib>:<oldVersion>' --type kt --type gradle` once per
     library to check for hardcoded pins.

## Checks

### A. Version sanity
- **No silent downgrade.** Compare the old and new `version` value as semver.
  A decrease (`2.0.0 -> 1.9.0`) or a snapshot regression (`-SNAPSHOT.183` ->
  `.182`) is a Must-fix unless the commit message explicitly justifies it.
- **Snapshot vs. release consistency.** If `version` switches from a release
  (`2.0.0`) to a snapshot (`2.0.1-SNAPSHOT.001`), confirm the consuming code
  isn't pinned to the release elsewhere via `grep -r '<libName>:<oldVersion>'`.
- **BOM ↔ component agreement.** For objects extending `DependencyWithBom`,
  check that `bom` references the same version as `version` (e.g. Kotlin's
  `kotlin-bom:$runtimeVersion`).

### B. Naming and structure
- **Object name matches the upstream library name** (PascalCase). New files
  must follow the convention of neighbors (e.g. `lib/Foo.kt` declares
  `object Foo`).
- **No type names in property names** (`fooList`, `barObject`) — this is in
  `.agents/coding-guidelines.md`.
- **Module constants use `"$group:<artifact>:$version"`**, not hardcoded
  Maven coordinates. Catch copy-paste like `"io.kotest:kotest-assertions-core:6.1.11"`.

### C. Deprecation discipline
When an artifact is **renamed or removed**:
- The old `const val` must stay with `@Deprecated("…", ReplaceWith("…"))`
  or `@Deprecated("…")` (see `Kotest.frameworkApi` and `Kotest.datatest` for
  the established style).
- If the diff deletes a `const val` outright, grep the repo with
  `git grep '<oldName>'` to confirm no caller is left behind. If callers exist,
  this is a Must-fix.

### D. Convention drift
- **Copyright header year.** Every changed file should have a current-year
  copyright line. If a file was edited but its copyright says `2024`, flag it
  (the user can run `/update-copyright` to fix).
- **GitHub URL comment.** New `lib/` and `kotlinx/` files conventionally
  start with `// https://github.com/<owner>/<repo>` above the object.
  Recommend it if missing.
- **`@Suppress("unused", "ConstPropertyName")` on the object.** This is the
  established style for constant-heavy declarations.

### E. Cross-cutting checks
- **`local/` deps don't leak.** Spine SDK artifacts in `local/` should not be
  declared in `lib/` or `test/` (and vice versa).
- **No mixing Groovy and Kotlin DSL.** All Gradle code in `buildSrc/` must be
  `.kt` or `.gradle.kts`. Catch any `.gradle` file slipping in.

## Output format

Three sections, in this order:

- **Must fix** — version downgrades, missing deprecation markers on removed
  symbols, broken callers, BOM/version mismatches.
- **Should fix** — convention drift, missing deprecation `ReplaceWith`,
  missing copyright update, missing URL comment, naming oddities.
- **Nits** — formatting, ordering, doc-comment polish.

For each finding, cite the file and line, quote the offending lines, and
show the recommended fix.

End with a one-line verdict: `APPROVE`, `APPROVE WITH CHANGES`, or
`REQUEST CHANGES`.
