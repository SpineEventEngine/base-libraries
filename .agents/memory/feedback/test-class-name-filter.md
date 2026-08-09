---
name: test-class-name-filter
description: Gradle `test` runs only classes named `*Test`/`*Spec`; others are skipped silently.
metadata:
  type: feedback
  since: 2026-08-08
---

The shared test convention (`registerTestTasks()` in
`buildSrc/src/main/kotlin/io/spine/gradle/testing/Tasks.kt`) filters test
execution to class names matching `*Test` or `*Spec` and sets
`filter.isFailOnNoMatchingTests = false`. A JUnit class with any other name
compiles, is reported as `BUILD SUCCESSFUL`, and never runs — even when
selected explicitly with `--tests`.

**Why:** During the Jackson 3 migration, a fixture-generating test named
`V2FixtureGeneration` silently did not run; the absence of its output files
was the only signal.

**How to apply:** Name every JUnit class `*Spec` (convention for specs) or
`*Test`. When a test run is expected to produce a side effect, verify the
side effect, not the exit code. If a `--tests` selection reports success
suspiciously fast, check `build/test-results/test/` for the class XML.
