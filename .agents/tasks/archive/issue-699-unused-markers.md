---
slug: issue-699-unused-markers
branch: increse-coverage
owner: claude
status: done-pending-commit
started: 2026-06-04
issue: https://github.com/SpineEventEngine/base-libraries/issues/699
---

## Goal

Issue #699 round 1: review the explicit `@SuppressWarnings("unused")
/* Part of the public API. */` markers left after the `base` split.
Either remove/hide the marked code, or cover it with tests.

## Finding

Org-wide verification (core-jvm, core-jvm-compiler, mc-java, base-types,
users + base internal/tests) showed **none of the 5 marked elements are
dead** — all are real public API:

| Element | Real consumer | Action |
|---|---|---|
| `FnStringifier` (class) | `base-types` `NetStringifier extends FnStringifier` | drop `"unused"`; keep `AbstractClassNeverImplemented` (no `main` subclass) |
| `EntityQuery.toRecordQuery()` | codegen DSL test; reachable from generated subclasses | drop `"unused"` (base test already covers it) |
| `EntityCriterion` (class) | emitted by codegen (`QueryColumn.kt` writes `new EntityCriterion<>(…)`) | drop `"unused"` (base test covers it) |
| `EntityQueryBuilder.withMask(SubscribableField…)` | generated query DSL | **added base test**, then dropped `"unused"` |
| `SortBy.direction()` | core-jvm runtime (`RecordComparator`, `EntityQueryToProto`, `ToEntityRecordQuery`) | **added base test**, then dropped `"unused"` |

`"unused"` on public members is an IDE-only inspection (javac/ErrorProne
do not warn), so removing it is build-safe.

## Changes

- `base/src/test/kotlin/io/spine/query/EntityQuerySpec.kt`: added
  `apply a field mask defined by subscribable fields` (covers the
  `withMask(SubscribableField…)` varargs overload via a stub
  `SubscribableField`) and `expose the sorting direction of its columns`
  (covers `SortBy.direction()` ASC/DESC).
- Removed `@SuppressWarnings("unused")` from `EntityQuery.toRecordQuery()`,
  `EntityCriterion`, `SortBy.direction()`, `EntityQueryBuilder.withMask(…)`,
  and dropped the `"unused"` entry from `FnStringifier` (kept
  `AbstractClassNeverImplemented`).

## Verification
`./gradlew :base:test` -> **889/889 pass** (was 887; +2 new tests).

## Kotlin `@Suppress("unused")` — reviewed, all legitimate (no change)

Swept all modules; only 3 in main sources, all justified and kept as-is
(maintainer decision):
- `type/ProtoTextExts.kt`, `type/JsonExts.kt`: `private const val ABOUT = ""`
  doc anchors (file-level KDoc attaches to them; referenced nowhere by
  design).
- `base/EntityState.kt`: phantom type parameter `<I>`, substituted by the
  CoreJvm Compiler in generated code (already self-documented).
Test-source `@Suppress("unused")` occurrences are normal JUnit/reflection
fixtures — out of scope.

## Out of scope / follow-ups (for later #699 rounds)
- The broad public->package-private over-exposure audit (~727 public
  members) remains untouched.
