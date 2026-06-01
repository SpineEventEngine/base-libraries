---
name: raise-coverage
description: >
  Raise JVM test coverage for a Gradle module or source path. Before anything
  else, ensures the repo is on Kover — if vanilla JaCoCo is detected, proposes
  a one-shot repo-wide migration and **waits for approval**. Then localizes
  uncovered lines and branches from Kover's JaCoCo-format XML report, and
  generates policy-compliant unit tests — stubs not mocks; tests are written
  in **Kotlin** with Kotest assertions, regardless of whether
  the code under test is Kotlin or Java; class names use the **`Spec`**
  suffix. Proposes a test-case list and waits for approval before writing any
  test, then re-runs the report to confirm the gap is closed. Use when asked
  to add missing tests, close coverage gaps, or raise a module's coverage.
---

# Raise test coverage

You localize untested code with **Kover**'s JaCoCo-format XML report and write
the unit tests that close the gap. Work on one Gradle module or path at a time,
always propose the test-case list and **wait for approval** before writing,
and verify the gap is actually closed afterward.

Before the main flow runs, you ensure the repo is on Kover. If vanilla JaCoCo
is detected anywhere, you propose a one-shot **repo-wide migration to Kover**
and wait for approval. The mechanical recipe lives in
[`references/migrate-to-kover.md`](references/migrate-to-kover.md).

The authoritative standards live in `.agents/`:

- `.agents/testing.md` — stubs not mocks; Kotest assertions; cover API edge
  cases; scaffold `when`/sealed-class branches.
- `.agents/coding-guidelines.md` — Kotlin/Java idioms for the tests you write.
- `.agents/version-policy.md` — tests-only changes do not require a version bump.

Mechanical detail (report paths, XML parsing, gap rules) lives in
[`references/coverage-signals.md`](references/coverage-signals.md). Keep this
file about *what to do*; that one is *how to read the numbers*.

## Scope

- **Coverage comes from Kover's local report.** Spine consumer repos apply the
  Kover Gradle plugin with `useJacoco(version = Jacoco.version)`, which makes
  Kover compute coverage with the JaCoCo engine and emit JaCoCo-format XML.
  Per-module task `:<module>:koverXmlReport`; XML at
  `<module>/build/reports/kover/report.xml`. KMP modules configured by Spine's
  `kmp-module` script plugin define only the `total` Kover report, so the
  same `koverXmlReport` / `report.xml` pair applies — see
  `references/coverage-signals.md`.
- **Target human-written `src/main` code only.** Never write tests for generated
  code (any path containing `generated`, e.g. Protobuf output), `examples`, or
  existing test sources. These are excluded by `.codecov.yml` — respect that
  boundary.
- **One module or path per run.**

## Inputs

`$ARGUMENTS` is one of:

- a Gradle module path — e.g. `:base`, `:core`;
- a source file or directory — e.g. `base/src/main/kotlin/io/spine/...`;
- `--triage` — read-only: produce a ranked gap report for the repo (or the named
  module) and stop, without proposing or writing tests.

If `$ARGUMENTS` is empty, ask which module or path to target (or offer
`--triage` to help choose).

## Step 0 — Ensure Kover

Run this **before** the Workflow below. Behaviour depends on `$ARGUMENTS`:

### Under `--triage` (read-only)

`--triage` is contractually read-only and must not write build files. If
Kover is not already applied everywhere, **emit a "Setup required" report
and stop** without writing anything (and without proposing a migration).
List the modules that still need migration, point at
[`references/migrate-to-kover.md`](references/migrate-to-kover.md), and tell
the user to re-run `/raise-coverage` **without** `--triage` to perform the
migration first. Once Kover is in place everywhere, `--triage` proceeds to
the Workflow.

### Otherwise

Branch on the repo's current coverage setup (detection patterns and full
migration recipe in
[`references/migrate-to-kover.md`](references/migrate-to-kover.md)):

1. **Kover applied everywhere already** — silently proceed to the Workflow.
2. **No coverage plugin anywhere** — silently install Kover (per the recipe).
   Record "Migration: installed Kover" in the final Report. No approval gate
   for this branch.
3. **Vanilla JaCoCo in ≥1 module** (with or without Kover alongside) — emit a
   proposal and **wait for approval** before making any edits.

### Proposal output

Emit the following Markdown sections, in this order, then stop and wait for approval:

- **Detected** — every module applying `jacoco` / `JacocoPlugin` /
  `JacocoConfig.applyTo` / a `jacoco-*.gradle.kts`; annotate "vanilla only"
  vs. "JaCoCo+Kover both"; note any root `jacocoRootReport`. Treat a root-level
  `KoverConfig.applyTo(rootProject)` as a Kover signal (it is the Kover-based
  successor to `JacocoConfig.applyTo`).
- **Plan** — every file that will be edited, with paths: per-module
  `build.gradle.kts`, root `build.gradle.kts`, `.codecov.yml`,
  `.github/workflows/*.yml`, `scripts/*.sh`.
- **Translation notes** — the rows from the translation table in
  `references/migrate-to-kover.md` that apply to this repo.
- **Manual-review surfaces** — items from that file's "Manual-review
  surfaces" list that the user must decide on before the migration can
  proceed.
- **Smoke check that will follow** — the commands listed in
  *Verify (smoke check)* below.
- Close with: "Confirm to apply, or call out anything to change first."

### Wait, then apply

Do not write any file until the user explicitly says "go" / "yes" / "apply"
(or equivalent). On adjustment requests, regenerate the proposal and wait
again. After approval, apply the migration per
`references/migrate-to-kover.md`, logging `edited <path>` per file. Any
unresolved manual-review surface → stop with "needs your call on `<x>`".

### Verify (smoke check)

Pick the smallest migrated leaf module and run `:<module>:koverXmlReport`,
then inspect `<module>/build/reports/kover/report.xml`. KMP modules also use
this task — Spine's `kmp-module` script plugin configures only Kover's
`total` report, which for the JVM-only KMP target is identical in shape to
the JVM case (see `references/migrate-to-kover.md` §6).

Run `./gradlew :<module>:koverXmlReport --quiet`; if the root was touched,
also run `./gradlew koverXmlReport --quiet`.
Confirm the XML exists, is non-empty, and the first non-XML-declaration line
contains `<report `. If a `DOCTYPE` is present, confirm it points at JaCoCo's
`report.dtd` — that confirms `useJacoco(...)` is in effect. Failure → stop;
do not fall through to the Workflow.

On success, **resume** at Workflow step 1.

## Workflow

1. **Resolve the target.**
   - A module/path → map it to its owning Gradle module (the project directory
     that contains it).
   - `--triage` → build the report — per-module `koverXmlReport` (or the
     root-level Kover aggregation task `koverXmlReport` if the repo wires
     one) — rank modules/files by uncovered %, output the ranked list, and
     **stop**.

2. **Localize the gaps** (per `references/coverage-signals.md`):
   - Run `:<module>:koverXmlReport` (the same task on JVM and KMP modules
     configured by Spine's convention plugins; see
     `references/coverage-signals.md`). The report task runs the tests first.
   - Parse the XML for uncovered lines (`ci == 0`) and partially covered
     branches (`mb > 0`). Prioritize methods whose `BRANCH` counter has
     `missed > 0`.
   - Drop any class under an excluded path (generated / examples / test).
   - Discard **non-actionable** gaps the engine cannot credit even with a
     perfect test (see `references/coverage-signals.md`): Kotlin `inline` /
     `inline reified` functions (their bytecode is inlined into each call
     site, so the definition lines stay `ci=0` regardless of tests),
     unreachable guards (`require`/`check`/`error` paths the public API
     cannot trigger), and `throw helper(...)` lines where the helper throws
     internally. Report these as non-actionable instead of proposing tests for
     them.

3. **Read before you write.**
   - Read the class(es) under test in full — public API, constructors, branch
     conditions, `when`/sealed exhaustiveness, error paths.
   - Read existing tests in the module to match structure, naming, fixtures,
     and the test source set/layout you will add to.
   - Read collaborators you will need to substitute, so you can write **stubs**
     (hand-written fakes), not mocks.

4. **Propose the test cases — then WAIT.**
   - For each target, list the concrete cases: the method/branch, the input,
     the expected outcome, and the stub(s) required. Map each case back to the
     uncovered line/branch it closes.
   - Present this list and **wait for the user's confirmation** before writing
     anything. (Under `--triage` you already stopped at step 1.)

5. **Generate the tests** (only after approval), per `.agents/testing.md`:
   - **Write tests in Kotlin**, regardless of whether the code under test is
     Kotlin or Java. Use JUnit Jupiter structure (`@Test` / `@Nested` /
     `@DisplayName`) with **Kotest assertions** (`shouldBe`, `shouldThrow`,
     `shouldContainExactlyInAnyOrder`, …). Reach for the
     `truth-proto-extension` only when asserting on Protobuf message subjects
     that Kotest's matchers cannot express, and keep that import isolated to
     the case that needs it.
   - **Class names use the `Spec` suffix** — e.g. `AbstractSourceFileSpec`,
     not `AbstractSourceFileTest`. This matches the house convention in
     existing `*Spec.kt` files (`base-libraries`, etc.) and applies even when
     the code under test is Java.
   - **Stubs, not mocks.** No mocking framework is on the classpath by design.
   - Cover API edge cases; add a case per `when`/sealed-class branch.
   - Place the test under `<module>/src/test/kotlin/...`, mirroring the
     package of the code under test (KMP: `src/jvmTest/kotlin/...` or
     `src/commonTest/kotlin/...` per the module's target). Reuse the file's
     copyright header.

6. **Verify.**
   - Re-run `:<module>:koverXmlReport`.
   - Confirm the previously-listed uncovered `nr` lines/branches no longer
     appear as gaps, and the class's `LINE` / `BRANCH` `missed` counters
     dropped.
   - Confirm the module total does not regress against `.codecov.yml`.
   - If a test fails to compile or the gap is not closed, fix and re-run before
     reporting done.

## Report

Return five sections (the **Migration** section is emitted only when Step 0
actually did work):

- **Migration** — what Step 0 changed, with the list of edited files and the
  smoke-check result. Omit when Step 0 was a no-op (Kover already in place).
- **Gaps** — uncovered lines/branches found (file → lines/branches).
- **Proposed cases** — the awaited list from step 4.
- **Generated** — test files added, with the cases each covers.
- **Verification** — before/after coverage for the target, and confirmation that
  no `.codecov.yml` target regressed.

## Safety

- **`--triage` is read-only.** Step 0 never writes under `--triage`; if
  Kover is not in place, emit "Setup required" and stop.
- **Migration requires approval when vanilla JaCoCo is detected.** Silent
  install of Kover happens only when *no* coverage frontend is in place and
  `--triage` is not requested.
- **Read-only until approval.** Do not write tests before the user confirms the
  step-4 list.
- **Never weaken a `.codecov.yml` target** or extend its `ignore` list to make a
  check pass.
- **Never add a mocking dependency** (Mockito, MockK, …) — write stubs.
- **No version bump.** Tests-only changes do not require one; do not invoke
  `/version-bumped` for a tests-only result. If you had to touch production code
  to make it testable, that is a separate change that needs its own review and a
  version bump. The migration itself (Step 0) **does** alter build files and is
  not tests-only — treat it as production-code change for version-bump purposes
  when it runs.
