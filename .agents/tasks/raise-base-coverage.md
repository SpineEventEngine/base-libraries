---
slug: raise-base-coverage
branch: increse-coverage
owner: codex
status: in-progress
started: 2026-06-01
related-memories: []
---

## Goal

Raise test coverage for the `:base` module using the `raise-coverage`
workflow. Success means Kover report gaps are localized, concrete test cases
are approved before implementation when actionable gaps exist, tests are added
in Kotlin with Kotest assertions and no mocks, and the follow-up Kover report
confirms targeted gaps closed without weakening Codecov settings.

## Context

- Target module: `:base`.
- Coverage source: `base/build/reports/kover/report.xml`.
- Kover is applied via the shared `module` plugin and root `KoverConfig`.
- Tests-only changes do not require a version bump.
- The skill requires an approval pause after proposing test cases.

## Plan

- [x] Load repo orientation, memory, testing rules, and coverage references.
- [x] Confirm Kover is already available for the target module.
- [x] Generate and parse the `:base` Kover XML report.
- [x] Read target sources and existing tests for selected gaps.
- [x] Document proposed concrete test cases for approval.
- [ ] Wait for approval to write tests.
- [ ] Add approved Kotlin `*Spec` tests using stubs, not mocks.
- [ ] Re-run `:base:koverXmlReport` and confirm targeted gaps closed.

## Findings

Step 0 found no migration work. Kover is already available through the shared
`module` plugin and root `KoverConfig`; no Gradle module inspected applies
vanilla JaCoCo.

Generated report:

- Command: `./gradlew :base:koverXmlReport --quiet`.
- XML: `base/build/reports/kover/report.xml`.
- Report format: JaCoCo XML emitted by Kover, with `report.dtd` DOCTYPE.
- Module totals: 3265/4212 lines covered (77.52%) and 750/1087 branches
  covered (69.00%).

Selected actionable gaps:

- `base/src/main/java/io/spine/code/fs/FsObject.java` — lines `53`, `64`,
  `71`, `77`, `82`, `87`, `92-99`; branches in `equals()`.
- `base/src/main/java/io/spine/code/fs/AbstractSourceFile.java` — lines
  `63-71`, `78-84`, `92-100`; branches in `lines()`.
- `base/src/main/java/io/spine/code/fs/SourceCodeDirectory.java` — lines
  `44-56`.
- `base/src/main/java/io/spine/code/fs/AbstractDirectory.java` — constructor
  lines `40-41`, covered by instantiating a concrete test subclass.
- `base/src/main/kotlin/io/spine/io/Files.kt` — lines `57-61`, `70-73`;
  branches in `File.toUnix()` and `File.toUnixPath()`.
- `base/src/main/kotlin/io/spine/io/Paths.kt` — lines `66-70`; branches in
  `Path.toUnix()`.

Non-actionable note:

- `AbstractSourceFile.java` lines `69` and `82` are `throw helper(...)` lines
  where the helper throws internally. They may remain as JaCoCo gaps even when
  the surrounding `IOException` paths are exercised; this is documented in the
  `raise-coverage` coverage-signals reference.

## Proposed Cases

Add `base/src/test/kotlin/io/spine/code/fs/FsObjectSpec.kt`:

- `FsObject` exposes path, parent, deprecated `directory()`, existence, and
  `toString()`.
  Input: a temp file and a missing sibling path.
  Expected: path and parent match, `directory()` delegates to `parent()`,
  the real file exists, the missing file does not, and `toString()` returns
  the path string.
  Closes: `FsObject.java` lines `53`, `64`, `71`, `77`, and `82`.

- `FsObject` equality and hash code are path-based.
  Input: two simple concrete subclasses with the same path, and another
  equality group with a different path.
  Expected: `EqualsTester().addEqualityGroup(...).testEquals()`.
  Closes: `FsObject.java` lines `87`, `92-99`, and the `equals()` branches.

- `SourceCodeDirectory` resolves child directories and source files.
  Input: tiny hand-written subclasses of `SourceCodeDirectory` and
  `AbstractSourceFile`.
  Expected: `root.resolve(child)` and `root.resolve(file)` return the resolved
  `Path`.
  Closes: `AbstractDirectory.java` constructor lines and
  `SourceCodeDirectory.java` lines `44-56`.

- `AbstractSourceFile` loads, exposes, updates, and stores lines.
  Input: a temp text file with two lines.
  Expected: `lines()` is empty before `load()`, loaded lines match file
  content, `update(...)` replaces the in-memory lines, and `store()` rewrites
  the file.
  Closes: `AbstractSourceFile.java` lines `63-68`, `78-80`, `92-100`, and both
  `lines()` branches.

- `AbstractSourceFile.load()` rejects a missing file.
  Input: non-existent temp path.
  Expected: `shouldThrow<IllegalStateException>`.
  Closes: the precondition path at `AbstractSourceFile.java:64`.

Extend `base/src/test/kotlin/io/spine/io/FilesSpec.kt`:

- `File.toUnix()` converts Windows-style separators and returns the same
  instance for Unix-style paths.
  Closes: `Files.kt` lines `57-61`.

- `File.toUnixPath()` converts Windows-style separators and returns the
  original path string otherwise.
  Closes: `Files.kt` lines `70-73`.

Extend `base/src/test/kotlin/io/spine/io/PathsSpec.kt`:

- `Path.toUnix()` converts Windows-style separators and returns the same
  instance for Unix-style paths.
  Closes: `Paths.kt` lines `66-70`.

All proposed tests are Kotlin `*Spec` tests, use Kotest assertions, and rely on
small hand-written subclasses rather than mocks.

## Log

- 2026-06-01 12:29 WEST — created task file; Kover is applied through
  `module` / `KoverConfig`, with no vanilla JaCoCo applied by the Gradle
  modules under inspection.
- 2026-06-01 12:30 WEST — generated `:base` Kover XML report. Module totals:
  3265/4212 lines (77.52%) and 750/1087 branches (69.00%). Selected focused,
  actionable gaps in `io.spine.code.fs` plus `File`/`Path` Unix conversion
  extension branches for the approval proposal.
- 2026-06-01 12:32 WEST — recorded the findings and proposed test cases in
  this plan document. Awaiting approval before writing tests.
