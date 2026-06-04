---
slug: raise-base-coverage
branch: increse-coverage
owner: codex
status: in-progress
started: 2026-06-01
related-memories: []
---

## Goal

Raise test coverage for the `:base` module while cleaning up stale public API.
Success means deprecated `:base` API is removed, `io.spine.code.fs` usage is
checked across SpineEventEngine projects, unused `io.spine.code.fs` types are
deprecated, the remaining non-deprecated API is covered with Kotlin tests using
Kotest assertions and no mocks, and the follow-up Kover report confirms targeted
gaps closed without weakening Codecov settings.

## Context

- Target module: `:base`.
- Coverage source: `base/build/reports/kover/report.xml`.
- Kover is applied via the shared `module` plugin and root `KoverConfig`.
- The original coverage-only plan would not require a version bump, but the
  updated scope includes production API removal/deprecation and therefore must
  be treated as a production-code change.
- The skill requires an approval pause after proposing test cases.
- `io.spine.code.fs` test coverage depends on the organization-wide usage
  analysis: only API that remains non-deprecated should be covered.

## Plan

- [x] Load repo orientation, memory, testing rules, and coverage references.
- [x] Confirm Kover is already available for the target module.
- [x] Generate and parse the `:base` Kover XML report.
- [x] Read target sources and existing tests for selected gaps.
- [x] Document proposed concrete test cases for approval.
- [x] Remove deprecated API in the `:base` module.
- [x] Analyze whether `io.spine.code.fs` is used in SpineEventEngine projects.
- [x] Mark `io.spine.code.fs` types that are not used by any project as deprecated.
- [x] Deprecate `io.spine.code.fs` types that are not used.
- [x] Analyse whether `RejectionType` is used in Spine SDK projects and deprecate if not.
- [x] Finalize test cases for the remaining non-deprecated API and wait for
  approval to write tests.
- [x] Add approved Kotlin `*Spec` tests using stubs, not mocks.
- [x] Re-run `:base:koverXmlReport` and confirm targeted gaps closed.

## Updated Scope

The plan now includes API cleanup before test generation:

- Remove deprecated API from `:base`. The first known target from the selected
  gaps is `FsObject.directory()`, which is deprecated in favor of `parent()`.
  The implementation pass must scan the whole `:base` module for other
  deprecated public API before editing.
- Analyze whether `io.spine.code.fs` is used by SpineEventEngine projects.
  Check the current repository first, then sibling/local checkouts and GitHub
  organization usage if local evidence is incomplete.
- Deprecate `io.spine.code.fs` types that have no organization usage. Do not
  add coverage for API newly marked as deprecated.
- Cover only the `io.spine.code.fs` API that remains non-deprecated after the
  usage analysis, plus the selected non-deprecated `File`/`Path` Unix
  conversion extension branches.

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

Selected actionable gaps before the updated API cleanup:

- `base/src/main/java/io/spine/code/fs/FsObject.java` — lines `53`, `64`,
  `71`, `77`, `82`, `87`, `92-99`; branches in `equals()`. Line `64`
  belongs to the deprecated `directory()` method and should be removed instead
  of covered.
- `base/src/main/java/io/spine/code/fs/AbstractSourceFile.java` — lines
  `63-71`, `78-84`, `92-100`; branches in `lines()`.
- `base/src/main/java/io/spine/code/fs/SourceCodeDirectory.java` — lines
  `44-56`.
- `base/src/main/java/io/spine/code/fs/AbstractDirectory.java` — constructor
  lines `40-41`, covered by instantiating a concrete test subclass.
- `base/src/main/kotlin/io/spine/io/Files.kt` — lines `57-61`, `70-73`;
  branches in `File.toUnix()` and `File.toUnixPath()`. The deprecated
  `File.toUnix()` API was removed, so only `File.toUnixPath()` remains for
  future coverage.
- `base/src/main/kotlin/io/spine/io/Paths.kt` — lines `66-70`; branches in
  `Path.toUnix()`.

Non-actionable note:

- `AbstractSourceFile.java` lines `69` and `82` are `throw helper(...)` lines
  where the helper throws internally. They may remain as JaCoCo gaps even when
  the surrounding `IOException` paths are exercised; this is documented in the
  `raise-coverage` coverage-signals reference.

## Deprecated API Removal

Removed from production sources:

- `io.spine.util.MoreCollections` deprecated aliases for
  `Iterable.theOnly()` and `Iterable.interlaced(...)`.
- `Indent.DEFAULT_SIZE`; `DEFAULT_JAVA_INDENT_SIZE` remains.
- `File.toUnix()`; `File.toUnixPath()` remains.
- `Any.unpackGuessingType()`; `Any.unpackKnownType()` remains.
- `Identifier.findField(...)`; `Field.findIdField(...)` remains.
- `Durations2.ZERO`, `Durations2.isPositive(...)`, and
  `Durations2.isNegative(...)`; Protobuf `Durations` replacements remain.
- `FsObject.directory()`; `FsObject.parent()` remains.
- `TypeUrl.toTypeName()`; `TypeUrl.typeName()` remains.
- `SourceFile.isRejections()`.
- Deprecated `CollectionsConverter` and its dedicated tests.
- Deprecated `Text` and its dedicated tests.

For `Columns`, the mutator overrides are required by `List`, so they were not
removed. Their local deprecation markers and `@deprecated` Javadocs were
removed; the methods still throw `UnsupportedOperationException` and remain
annotated with `@DoNotCall`.

Verification after removal:

- `rg -n "@Deprecated|Deprecated\\(|@deprecated" base/src/main base/src/test`
  returned no matches.
- `./gradlew :base:build --quiet` passed.

## Proposed Cases

These cases are provisional until the `io.spine.code.fs` usage analysis is
complete. Cases for API that becomes deprecated must be dropped; cases for API
that remains supported should be implemented.

Add `base/src/test/kotlin/io/spine/code/fs/FsObjectSpec.kt`:

- `FsObject` exposes path, parent, existence, and `toString()`.
  Input: a temp file and a missing sibling path.
  Expected: path and parent match, the real file exists, the missing file does
  not, and `toString()` returns the path string.
  Closes: `FsObject.java` lines `53`, `71`, `77`, and `82`.

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

- `File.toUnixPath()` converts Windows-style separators and returns the
  original path string otherwise.
  Closes: `Files.kt` lines `70-73`.

Extend `base/src/test/kotlin/io/spine/io/PathsSpec.kt`:

- `Path.toUnix()` converts Windows-style separators and returns the same
  instance for Unix-style paths.
  Closes: `Paths.kt` lines `66-70`.

All proposed tests are Kotlin `*Spec` tests, use Kotest assertions, and rely on
small hand-written subclasses rather than mocks. Do not test removed or newly
deprecated API.

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
- 2026-06-01 12:34 WEST — updated scope per user request: remove deprecated
  `:base` API, analyze `io.spine.code.fs` usage across SpineEventEngine
  projects, deprecate unused types, and cover only the non-deprecated API.
- 2026-06-01 12:58 WEST — removed deprecated `:base` APIs, deleted dedicated
  tests for removed deprecated types, refreshed copyright headers on modified
  source files, confirmed no deprecated markers remain under `base/src`, and
  passed `./gradlew :base:build --quiet`.
