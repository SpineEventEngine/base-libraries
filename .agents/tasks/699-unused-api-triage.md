---
slug: 699-unused-api-triage
branch: unused-api
owner: claude
status: in-progress
started: 2026-06-07
---

## Goal

Triage the public API of `base-libraries` for unused and over-exposed types per
[issue #699](https://github.com/SpineEventEngine/base-libraries/issues/699), and
produce a categorized, evidence-backed list of removal / hide / test candidates.
**This pass is a report only — no source changes.**

## Context

After `base` was split into several repositories, some types/methods became
unused and many endpoints were left over-exposed (`public` where package-private
or `internal` would do). During the Java 11 migration, "unused" warnings were
silenced with `@SuppressWarnings("unused") /* Part of the public API. */` rather
than resolved.

**Headline:** the codebase is in much better shape than the issue (filed before
the post-split cleanup) implies. The Java `@SuppressWarnings("unused")` markers
are already gone from `src/main`; the three surviving `@Suppress("unused")`
markers are all legitimate. The remaining actionable surface is **17
over-exposed public types** and **1 redundant public `typealias`**.

## Methodology

Enumerated all 269 public/internal top-level types across the four modules'
`src/main`, then measured references with a single-pass token scan
(`\b<Name>\b`) over every Spine repo checked out locally
(`/Users/sanders/Projects/Spine`, ~63 k source files), excluding
`build/`, `generated/`, and Gradle caches. Each match was classified into:

1. **External** — a consumer repo (`core-jvm`, `validation`, `ProtoData`, …).
2. **Other module** — a different `base-libraries` module.
3. **Same module** — another file in the defining module.
4. **Test** — the defining module's `src/test`.

Buckets: **KEEP** (external or other-module use), **REDUCE VISIBILITY**
(same-module/test use only — over-exposed if currently `public`), **UNUSED** (no
reference anywhere), **TEST-ONLY**. Over-exposed and unused candidates were then
verified by reading the declaration and by a fresh grep (including generated
code) for subclasses/consumers.

### Caveats and false-positive guards

- **Reflective SPI is invisible to a token scan.** Two classes are registered
  via `@AutoService` and loaded through `ServiceLoader` — `BaseOptionsProvider`
  (`OptionsProvider`) and `ProtoComparators` (`ComparatorProvider`). Both look
  "unused" to the scan but are **KEEP**. These are the only two `@AutoService`
  classes in the repo.
- **"Unused" means unused by the locally checked-out repos.** Artifacts here are
  published to Maven; a type with no local consumer may still be deliberate
  public API for third parties. Over-exposed candidates are therefore
  *candidates to verify*, not confirmed-safe edits.
- **Private file-local symbols** (e.g. `TextOutput`, `JsonOutput`, `AsIs`) are
  used within their own file, which the scan excludes, so they show as "unused"
  but are not — the compiler would flag a genuinely unused `private` symbol.
- **Extension-function files** are matched by receiver type, not function name,
  so their exact per-function usage is imprecise; all are heavily used utilities
  and resolve to KEEP regardless.
- **Member-level over-exposure** (individual `public` methods that could be
  narrowed) is out of scope for this pass — see follow-ups.

## Summary

| Module | Types | KEEP | Over-exposed (public→reduce) | Unused public | Notes |
|---|---:|---:|---:|---:|---|
| annotations | 9 | 9 | 0 | 0 | clean |
| environment | 9 | 9 | 0 | 0 | clean |
| format | 17 | 15 | 2 | 0 | `JacksonSupport`, `JacksonWriter` |
| base | 234 | 219 | 15 | 1 | `ViewState` typealias |
| **total** | **269** | **252** | **17** | **1** | |

## Decisions (reviewed 2026-06-07, one-by-one with the API owner)

The 17 over-exposed candidates were reviewed individually. Outcome:

- **Reduce to `internal` (applied):** `TypeRegistryHolder`, `ExtensionRegistryHolder`
  — Kotlin `object`s used only inside the `base` module, no external use, no
  direct test. Now `internal`; in-module callers unchanged, covered transitively.
- **Keep public + add test (applied):** `RecordCriterion` — kept public as part of
  the record-query DSL; added [`RecordCriterionSpec`](../../base/src/test/kotlin/io/spine/query/RecordCriterionSpec.kt)
  to name and exercise it explicitly (it was already covered behaviourally via
  `.where(col).is(val)` in `RecordQueryBuilderTest`, just never referenced by name).
- **Keep public as-is (the other 14):** all are deliberate public surfaces —
  `@SPI` extension points (`JacksonSupport`, `JacksonWriter`, `RecordSubjectParameter`),
  the record-query DSL (`CustomSubjectParameter`, `RecordPredicates`), the option
  model (`AbstractOption`, `FieldOption`), value/type-model API forced public by
  signatures or extension (`NestedTypeName` — return type of `Type.nestedSimpleName()`;
  `ClassTypeValue`, `ComparableStringValue`; `AbstractFieldName` — cross-package base),
  the thrown exception `MissingStringifierException`, the cross-package Java util
  `Predicates2`, and the `PubPreconditions` set (`requireInternal`). All are tested
  (directly or transitively).

Separately, the unused `ViewState` typealias (Finding B) was **kept** and is now
exercised by a test in [`EntityStateSpec`](../../base/src/test/kotlin/io/spine/base/EntityStateSpec.kt).

## Findings

### A. Over-exposed public types → reduce visibility (17)

Each is `public` but referenced **only within its own module and that module's
tests** — zero references in any other module or consumer repo (verified,
including generated code, no subclasses found outside `base-libraries` tests).
Recommended action: narrow to package-private (Java) / `internal` (Kotlin),
**after** confirming no published external consumer relies on it.

The `abstract` types (`AbstractOption`, `ClassTypeValue`, `ComparableStringValue`,
`AbstractFieldName`, `JacksonSupport`/`JacksonWriter`) read like intended
extension points — confirm intent before narrowing; if they are genuinely
internal base classes, reducing visibility is the right call.

| Type | File | same-mod / test refs |
|---|---|---|
| `JacksonSupport` (abstract) | [format/…/format/JacksonSupport.kt](format/src/main/kotlin/io/spine/format/JacksonSupport.kt) | 2 / 0 |
| `JacksonWriter` (abstract) | [format/…/format/write/JacksonWriter.kt](format/src/main/kotlin/io/spine/format/write/JacksonWriter.kt) | 2 / 0 |
| `AbstractFieldName` | [base/…/code/AbstractFieldName.java](base/src/main/java/io/spine/code/AbstractFieldName.java) | 1 / 0 |
| `AbstractOption` (abstract) | [base/…/code/proto/AbstractOption.java](base/src/main/java/io/spine/code/proto/AbstractOption.java) | 2 / 1 |
| `FieldOption` | [base/…/code/proto/FieldOption.java](base/src/main/java/io/spine/code/proto/FieldOption.java) | 2 / 1 |
| `RecordSubjectParameter` | [base/…/query/RecordSubjectParameter.java](base/src/main/java/io/spine/query/RecordSubjectParameter.java) | 3 / 3 |
| `RecordCriterion` | [base/…/query/RecordCriterion.java](base/src/main/java/io/spine/query/RecordCriterion.java) | 1 / 0 |
| `CustomSubjectParameter` | [base/…/query/CustomSubjectParameter.java](base/src/main/java/io/spine/query/CustomSubjectParameter.java) | 8 / 2 |
| `RecordPredicates` (interface) | [base/…/query/RecordPredicates.java](base/src/main/java/io/spine/query/RecordPredicates.java) | 1 / 1 |
| `MissingStringifierException` | [base/…/string/MissingStringifierException.java](base/src/main/java/io/spine/string/MissingStringifierException.java) | 2 / 1 |
| `NestedTypeName` | [base/…/type/NestedTypeName.java](base/src/main/java/io/spine/type/NestedTypeName.java) | 1 / 1 |
| `requireInternal` (fun) | [base/…/type/PubPreconditions.kt](base/src/main/kotlin/io/spine/type/PubPreconditions.kt) | 1 / 1 |
| `TypeRegistryHolder` (object) | [base/…/type/TypeRegistryHolder.kt](base/src/main/kotlin/io/spine/type/TypeRegistryHolder.kt) | 2 / 0 |
| `ExtensionRegistryHolder` (object) | [base/…/type/ExtensionRegistryHolder.kt](base/src/main/kotlin/io/spine/type/ExtensionRegistryHolder.kt) | 3 / 0 |
| `Predicates2` | [base/…/util/Predicates2.java](base/src/main/java/io/spine/util/Predicates2.java) | 2 / 1 |
| `ClassTypeValue` (abstract) | [base/…/value/ClassTypeValue.java](base/src/main/java/io/spine/value/ClassTypeValue.java) | 1 / 1 |
| `ComparableStringValue` (abstract) | [base/…/value/ComparableStringValue.java](base/src/main/java/io/spine/value/ComparableStringValue.java) | 1 / 1 |

> Note: `TypeRegistryHolder` / `ExtensionRegistryHolder` are `object`s with
> `test=0` — used only by production code in their own module, never by a test.
> If kept public, they would benefit from direct tests per the issue; if made
> `internal`, they are already covered transitively.

### B. Unused public type → remove (or keep deliberately) (1)

- `ViewState<I>` — `public typealias ViewState<I> = ProjectionState<I>` in
  [base/…/base/EntityState.kt:87](base/src/main/kotlin/io/spine/base/EntityState.kt).
  Its KDoc is just "Same as [ProjectionState]." Zero references anywhere
  (local repos, tests, generated code). **Recommend removal** as a redundant
  alias, unless it is intentionally retained as a published-API synonym.

### C. `@Suppress("unused")` marker audit — all legitimate, no action

| Marker | Location | Verdict |
|---|---|---|
| `@Suppress("unused")` on `interface EntityState<I>` | [EntityState.kt:61](base/src/main/kotlin/io/spine/base/EntityState.kt) | Justified — `<I>` is consumed by generated code. |
| `@Suppress("unused")` on `private const val ABOUT = ""` | [ProtoTextExts.kt:47](base/src/main/kotlin/io/spine/type/ProtoTextExts.kt) | Justified — doc-anchor idiom (carries file KDoc). |
| `@Suppress("unused")` on `private const val ABOUT = ""` | [JsonExts.kt:54](base/src/main/kotlin/io/spine/type/JsonExts.kt) | Justified — same doc-anchor idiom. |

No Java `@SuppressWarnings("unused")` remains in any module's `src/main`.

### D. Confirmed non-findings (guarded false positives)

- `BaseOptionsProvider`, `ProtoComparators` — `@AutoService` SPI providers, KEEP.
- `TextOutput`, `JsonOutput`, `DefaultInstanceValue`, `ProtobufEscapeSequences`,
  `AsIs`, `readFile` — `private`, used within their own file. KEEP.
- 34 package-private Java types flagged "same-module-only" are already correctly
  scoped (not `public`) — no over-exposure.
- `annotations` and `environment` modules: clean, every public type used by
  external consumers.

## Proposed follow-ups (separate PRs, after user triage)

1. **`format` over-exposure** (low risk, self-contained): narrow `JacksonSupport`
   / `JacksonWriter` to non-public. Smallest, safest first PR.
2. **`base` over-exposure** (medium risk): the 15 `base` candidates, grouped by
   package (`query`, `value`, `code.proto`, `type`, …); confirm each abstract
   type's extension-point intent first.
3. **`ViewState` removal** (or explicit keep decision).
4. **Member-level pass** (out of scope here): review `public` *methods* on
   otherwise-KEEP types for over-exposure — a deeper, separate analysis.

## Verification performed

- Anchors behaved as expected: `Format`, `ProtoJson`, `EntityState` (ext 157),
  `Message`/`Class` extensions → KEEP; `JacksonSupport`/`JacksonWriter` → REDUCE
  (0 external, manually re-grepped); `ViewState` → UNUSED (0 refs, confirmed).
- All 17 over-exposed candidates re-verified with an independent
  `grep -lw … | grep -v base-libraries/` pass → 0 external files each.
- Subclass check (including generated code) for every `abstract` candidate → no
  consumer subclasses outside `base-libraries` tests.
- SPI blind spot closed by enumerating all `@AutoService` classes (2, both KEEP).

## Out of scope / untouched

The pre-existing staged changes under `buildSrc/` and the `config` submodule are
unrelated to #699 and were left as-is. No build was run (report-only pass).
