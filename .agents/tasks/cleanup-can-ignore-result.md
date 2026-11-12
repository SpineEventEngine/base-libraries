---
slug: cleanup-can-ignore-result
branch: claude/cleanup-not-thrown-759bef
owner: claude
status: in-review
started: 2026-11-12
---

## Goal

Java code that already holds a failure calls `runKeepingFailure(e, step); throw e;`
without IDEA reporting `Result of 'runKeepingFailure()' not thrown`, and with no
suppressions at the call sites.

## Context

- IDEA reports two problems at such a call site: `ThrowableNotThrown` (WARNING)
  and `ResultOfMethodCallIgnored` (INFO, from the `@CheckReturnValue` of the
  `io.spine.util` package). Both were seen in core-jvm,
  `BoundedContext.checkNoRepositories()`.
- Per the IntelliJ Community sources, `var unused = runKeepingFailure(…)` is still
  reported: a local which is never read counts as ignored, whatever its name.
  A method-level `@CanIgnoreReturnValue` silences both inspections.
- The owner chose `@CanIgnoreReturnValue` over a new `void` function. The cost:
  IDEA no longer warns when the result is ignored with no prior failure. That loses
  the failure of the step. ErrorProne's `CheckReturnValue` is off in Spine builds,
  so the KDoc is the remaining guard.

## Plan

- [x] Annotate `runKeepingFailure()` with `@CanIgnoreReturnValue`.
- [x] Update the "Cleaning up after a failure" KDoc section: drop `var unused`,
      say when the result can be ignored.
- [x] `CleanupJavaSpec`: call `runKeepingFailure()` as a statement, as core-jvm does.
- [x] `CleanupSpec`: cover the rethrow shape with the result ignored.
- [x] Bump the version to `2.0.0-SNAPSHOT.465` (working tree only).
- [x] `./gradlew build dokkaGenerate`; check the annotation in the bytecode.
- [x] Pre-PR reviews (`kotlin-engineer`, `spine-code-review`, `review-docs`): approved.

## Log

- 2026-11-12 — owner picked `@CanIgnoreReturnValue`; plan approved, executing.
- 2026-11-12 — build and `dokkaGenerate` pass; reviewers approved, wording fixes
  applied. Awaiting the owner's OK to commit and open the PR.
