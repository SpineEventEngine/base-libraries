---
slug: add-suppliers2-iterators2
branch: add-suppliers2-and-iterators2
owner: claude
status: in-review
started: 2026-07-16
---

## Goal

`Suppliers2` and `Iterators2`, introduced in [core-jvm PR #1656][pr] under
`io.spine.server`, live in this repo's `io.spine.util` package (module `base`)
with test coverage, so that all Spine SDK repos can share them and `core-jvm`
can later drop its copies.

## Context

- Both classes are nullness-friendly wrappers over Guava's `Suppliers.memoize`
  and `Iterators.filter`: Guava declares `<T extends @Nullable Object>`, which
  reads as a nullness mismatch when the result is assigned to a non-null type
  in `@NullMarked` code. Declaring plain `<T>` in a `@NullMarked` package binds
  `T` to a non-null type.
- `io.spine.util` already hosts the Guava-companion naming pattern
  (`Preconditions2`, `Predicates2`) and is `@NullMarked` via `package-info.java`.
- The classes keep `@Internal` (as in the source PR): they are framework
  plumbing; widening to public API later is non-breaking, the reverse is not.
- New tests follow `kotlin-jvm-tester` conventions: Kotlin, `Spec` suffix,
  `internal`, `UtilityClassTest` base (private ctor + final +
  `NullPointerTester` on public statics), Kotest assertions.

[pr]: https://github.com/SpineEventEngine/core-jvm/pull/1656

## Plan

- [x] Add `base/src/main/java/io/spine/util/Suppliers2.java` — verbatim from
      the PR, only the `package` line changes.
- [x] Add `base/src/main/java/io/spine/util/Iterators2.java` — same.
- [x] Add `base/src/test/kotlin/io/spine/util/Suppliers2Spec.kt` — delegate
      value returned; laziness; delegate called once.
- [x] Add `base/src/test/kotlin/io/spine/util/Iterators2Spec.kt` — retains
      matching elements in order; exhausted when none match; `remove()`
      unsupported.
- [x] Verify: `./gradlew build dokkaGenerate` (JDK 17 via `JAVA_HOME`).
- [ ] PR: branch off `master`, bump `version.gradle.kts`
      (`2.0.0-SNAPSHOT.424` → `.425`), run `pre-pr`, open the PR.

## Log

- 2026-07-16 16:45 — drafted and validated (build commands, `UtilityClassTest`
  contract, detekt impact); executing.
- 2026-07-16 16:55 — all files in place; scoped specs green (11 tests,
  0 failures); full `./gradlew build dokkaGenerate` BUILD SUCCESSFUL in 30s.
  `spine-code-review` + `kotlin-engineer` review passes launched.
- 2026-07-16 17:14 — version bumped to `.425`, reports regenerated,
  pre-PR gate PASS (3 reviewers approve); PR #953 opened.
- 2026-07-16 17:35 — `review-docs` nits addressed: `@return` tags on both
  methods, labeled `Suppliers.memoize()` link, timed `Log` entries.
