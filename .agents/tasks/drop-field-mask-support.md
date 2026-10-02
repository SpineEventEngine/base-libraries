---
slug: drop-field-mask-support
branch: drop-field-mask-support
owner: claude
status: in-review
started: 2026-10-02
---

## Goal

Field masks are no longer supported in queries and subscriptions. In `spine-base`, every
`io.spine.query` API that accepts or returns a `FieldMask` stays (binary compatibility),
but is **deprecated and does nothing**. Builders ignore masks, and queries always report
the default (empty) mask, so the query results always contain all the fields of the records.

## Context

- Counterpart of the `core-jvm` branch `drop-support-of-field-mask`; its task plan lists
  this repo as a follow-up: deprecate `QueryBuilder.withMask(..)`, `whichMask()`,
  `Query.mask()`; update the `io.spine.query` package docs.
- The version is already bumped on this branch (`2.0.0-SNAPSHOT.450`, `master` has `.445`).
- `jdbc-storage` (`SelectMessagesByQuery`), `gcloud-jvm` (`FieldMaskApplier`) and
  `delivery-server` (Redis `TenantRecords.readAll`) skip masking when `RecordQuery.mask()`
  is the default instance. With `mask()` always returning the default, they stop masking
  with no changes on their side (only deprecation warnings).
- Out of scope (not query/subscription features, consistent with `core-jvm` keeping its
  own `FieldMasks`):
  - `io.spine.protobuf.FieldMasks` (`FieldMaskExts.kt`) — generic Protobuf helpers.
  - `field_filter.proto` — mentions `FieldMask.paths` only to describe the path syntax.

## Deprecation conventions (same as `core-jvm`)

- `@Deprecated` plus a `@deprecated` Javadoc tag on every deprecated element, including
  overrides: callers via `RecordQueryBuilder` resolve to `AbstractQueryBuilder.withMask(..)`,
  so the overrides in `AbstractQueryBuilder` must carry `@Deprecated` for the callers to
  be warned. Checkstyle `MissingDeprecated` checks any Javadoc present.
- Message: "Field masks are no longer supported. The query results always contain all
  the fields of the records. Please remove the call." Summary line: "Does nothing."
- No-op bodies keep `checkNotNull(..)` on their arguments and store nothing.
- Tests touching the deprecated API use `@Suppress("DEPRECATION")` at the narrowest scope.

## Plan

- [x] `Query.mask()` → deprecated `default` method returning `FieldMask.getDefaultInstance()`.
- [x] `QueryBuilder.whichMask()` → deprecated `default` method returning `Optional.empty()`.
- [x] `QueryBuilder.withMask(FieldMask | String... | Field...)` → deprecated, "Does nothing".
- [x] `AbstractQueryBuilder`: remove the `mask` field, the `whichMask()` override and the
      package-private `withMask(Collection<String>)`. The three public `withMask(..)`
      become deprecated no-ops: `checkNotNull(..)`, then `return thisRef()`. The
      "top level only" check is dropped — there is nothing left to guard.
- [x] `EntityQueryBuilder.withMask(SubscribableField...)` → deprecated no-op.
- [x] `AbstractQuery`: remove the `mask` field and the `mask()` override; drop the mask
      from `toString()`, `equals()`, `hashCode()`.
- [x] `EntityQuery`: remove `copyMask(..)` used by `toRecordQuery()` and `copyTo(..)`.
- [x] `RecordQuery.and(..)`/`either(..)` docs: drop "field masks" from the ignored parts.
- [x] `package-info.java`: remove both "Field masks" sections.
- [x] Remove imports left unused by the above — no configured tool catches them.
- [x] Tests (new cases in Kotlin, per the testing policy):
  - `RecordQueryBuilderTest.java`: remove `withFieldMask`, `withMaskPaths`, `ofFieldMask`.
  - `given/RecordQueryBuilderTestEnv.java`: `assertNoSortingMaskLimit` →
    `assertNoSortingAndLimit` (no mask assertion); keep `fieldMaskWith(..)` for the new spec.
  - `given/RecordQueryTestEnv.java` + `RecordQueryTest.java`: drop incidental
    `.withMask(..)`; `withMaskSortingAndLimit` → `withSortingAndLimit`.
  - **New** `base/src/test/kotlin/io/spine/query/RecordQueryBuilderSpec.kt`, group
    `ignore field masks`: every overload returns the same builder; `whichMask()` is empty
    and `RecordQuery.mask()` is the default instance (the SPI contract storages rely on);
    a query with a mask equals (and hashes as) one without; unknown paths and masks
    inside `either(..)` no longer throw.
  - `EntityQuerySpec.kt`: drop the mask from `be converted to 'RecordQuery'` and the
    incidental `withMask(..)` in `apply a custom column criterion`; replace
    `apply a field mask defined by subscribable fields` with
    `ignore a field mask defined by subscribable fields`.
- [x] Verify: `./gradlew build dokkaGenerate`; no `[deprecation]` warnings from
      `:base:compileJava`/`:base:compileTestJava`, Kotlin ones only inside `@Suppress`;
      grep gate — no `FieldMaskUtil`, `copyMask` or stored mask in
      `base/src/main/java/io/spine/query`.
- [x] Review the diff with `spine-code-review` and `review-docs`.

## Behavior changes worth a release note

- `withMask(..)` inside `either(..)` no longer throws `IllegalStateException`.
- Unknown mask paths are no longer rejected with `IllegalArgumentException`.
- `null` elements inside the `withMask(..)` varargs are no longer rejected.
- Queries that differed only by their masks are now equal and have equal hash codes;
  `toString()` of a query no longer prints a mask.

## Follow-ups (other repos, not in this change)

- `core-jvm-compiler`: `tests/entity-queries/.../io/spine/query/EntityQueryBuilderTest.java`
  asserts that masks are applied (`withFieldMask`, `withMaskPaths`,
  `withMaskDefinedBySubscribableFields`, `withMaskDefinedByFields`, `ofFieldMask`); these
  fail once it depends on this version. `EntityQueryTest`, `EntityQueryTestEnv` and
  `EntityQueryBuilderTestEnv` become deprecated callers. Also drop `.withMask(..)` from the
  generated-code example in `entity/.../query/QueryBuilderClass.kt`.
- `core-jvm`: callers of `RecordQueryBuilder.withMask(..)`/`Query.mask()` (incl. tests
  and test fixtures) get deprecation warnings after bumping `spine-base`.
- `jdbc-storage`, `gcloud-jvm`, `delivery-server` (Redis `TenantRecords`): remove
  the now-dead masking of `RecordQuery.mask()`.

## Log

- 2026-10-02 — analyzed; drafted, awaiting approval
- 2026-10-02 — approved (plan refined after an adversarial design review); executing
- 2026-10-02 — implemented; `./gradlew build dokkaGenerate` green (JDK 17). New
  `RecordQueryBuilderSpec` (4) and `EntityQuerySpec` (8) pass; only javac deprecation
  warnings are 3 pre-existing ones in generated `OptionsProto.java`; no Kotlin warnings.
- 2026-10-02 — reviewed: `spine-code-review` and `review-docs` approve. Applied: split
  `without validating them` into `with unknown paths` / `set inside 'either()'`, hoisted
  repeated mask inputs, env Javadoc, task-file facts (`delivery-server`, equality note).
  Re-ran: 135 `io.spine.query` tests and `:base:detekt` green.
- 2026-10-02 — pre-PR round: `spine-code-review`, `review-docs` and `kotlin-engineer`
  approve. Applied their nits: a `null` test for `withMask(FieldMask)`, `masked`/`unmasked`
  names, a sorting assertion replacing an always-true `shouldNotBe null`, suppression
  comments that say when to delete the tests. Not tested: `null` arrays passed to the
  varargs overloads — spreading a `null` array from Kotlin throws before the call, so such
  a test would pass without reaching the precondition.
