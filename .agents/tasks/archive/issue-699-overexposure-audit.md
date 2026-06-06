---
slug: issue-699-overexposure-audit
branch: increse-coverage
owner: claude
status: analysis-only
started: 2026-06-04
issue: https://github.com/SpineEventEngine/base-libraries/issues/699
---

## Scope

Type-level over-exposure audit of the `base` module: which **public
top-level Java types** are referenced *only within their own package*
across the whole org (→ could be package-private), vs cross-package within
`base` but not externally (→ `@Internal`), vs genuinely consumed
externally (→ keep public).

Member-level over-exposure (~727 public members) is NOT covered — grep
can't reliably resolve method/field usage or overloads. That needs the
IntelliJ "package-private could be used" / "weakest access" inspection.

## Method

- 117 public top-level Java types in `base/src/main/java`.
- Cross-package usage within `base` computed by FQN match (regular AND
  static imports, inline FQN).
- Org-wide consumer check via clone-and-grep of: core-jvm, core-jvm-compiler,
  mc-java, compiler, model-compiler, ProtoTap, cli, bootstrap, template,
  validation, base-types, time, change, money, web, users, gcloud-java,
  jdbc-storage + local tool-base, mc-js. FQN-substring match (catches
  static imports and codegen emitter references).

## Result

- **~46 types** are genuinely consumed externally (core-jvm runtime,
  generated code via mc-java / core-jvm-compiler emitters, base-types,
  etc.) → **keep public**. (e.g. `Identifier`, `EventMessage`, `Errors`,
  `Query`, `EntityQuery`, `Column`, `Stringifier`, `Exceptions`,
  `Durations2`, `FnStringifier`, `EntityCriterion`, `IdCriterion`, …)
- **21 types** are NOT referenced by any swept external repo. Of those,
  partitioned by base-internal cross-package use:

### A. Package-private candidates (no cross-package use anywhere) — 14
io.spine.base.IdType
io.spine.io.Ensure
io.spine.option.BaseOptionsProvider
io.spine.protobuf.Diff
io.spine.protobuf.MessageFieldException
io.spine.query.CustomSubjectParameter
io.spine.query.IdParameter
io.spine.query.RecordCriterion
io.spine.query.RecordSubjectParameter
io.spine.string.MissingStringifierException
io.spine.type.ApiOption
io.spine.type.NestedTypeName
io.spine.util.Duplicates
io.spine.util.Math2

### B. `@Internal` candidates (cross-package within base, not external) — 2
io.spine.io.IoPreconditions   (3 base packages use it via static import)
io.spine.util.Predicates2     (2 base packages)

### C. Special cases (NOT simple candidates)
- `io.spine.code.proto.AbstractOption`, `io.spine.code.proto.MessageOption`
  — superclasses of externally-used public option types (`FieldOption`→
  `ColumnOption`; `EntityStateOption`). Leaving public (reducing a
  supertype of public API is the risky public-subclass-of-package-private
  pattern). Consistent with the proto-round decision.
- `io.spine.code.proto.FileOption` — genuinely unused leaf (no subclass).
  Package-private or removal candidate; kept for option-family symmetry.
- `io.spine.base.RejectionType` — already `@Deprecated` for removal →
  handle via deprecation removal, not visibility.
- `io.spine.code.proto.FieldTypes` — used by **mc-js** (static import) →
  keep public (NOT a candidate; corrected after FQN-substring recheck).

## Caveats before acting
1. **Public-signature exposure:** a package-private candidate must also not
   appear as a return/param type of a public method on a public same-package
   type (callers can use it via `var`/chaining without importing it). The
   query `*Parameter`/`*Criterion` types carry the highest such risk — each
   needs a compile-checked verification at tightening time.
2. **Unswept repos:** ~25 app/infra repos (auth, logging, dashboard,
   deployment, chat-bot, examples, message-delivery, publishing, reflect,
   site-*, …) were not cloned — unlikely to use these low-level types, but
   not verified.
3. Tightening visibility is a source-compatibility change for any
   out-of-org consumer (same caveat as the fs/proto moves).

## Applied (low-risk subset, build-verified — `:base:test` green)

Per-type diligence (same-package public-signature exposure + SPI checks)
shrank the "safe" set considerably:

- [x] `io.spine.base.IdType` -> **package-private** (used by `Identifier`
      only via package-private methods `type()`/`toType()`; fixed the
      `[IdType]` KDoc link in `EntityState` to a plain code span so public
      docs don't link a hidden type).
- [x] `io.spine.io.IoPreconditions` -> **`@Internal`** (cross-package base
      utility).
- [x] `io.spine.util.Predicates2` -> **`@Internal`** (cross-package base
      utility).

### Disqualified by diligence (NOT changed)
- `io.spine.option.BaseOptionsProvider` — `@AutoService(OptionsProvider.class)`,
  discovered via `ServiceLoader.load(...)`. Must stay **public** (package-
  private would break runtime SPI; compiler would NOT catch it).
- `io.spine.type.NestedTypeName` — returned by the **public**
  `Type.nestedSimpleName()` (and `Type` is heavily used externally). Leave
  public (hiding it would silently narrow the public API).

### Judgment call — deferred (tested public utilities/exceptions, no SDK
### consumer and no external use; hiding removes them from the published
### library API)
`io.spine.io.Ensure`, `io.spine.protobuf.Diff`,
`io.spine.protobuf.MessageFieldException`,
`io.spine.string.MissingStringifierException`, `io.spine.type.ApiOption`,
`io.spine.util.Duplicates`, `io.spine.util.Math2`
(+ the 4 query `*Parameter`/`*Criterion` internals deferred earlier).

## One-by-one review outcomes (complete)

Each not-used-outside-base candidate was reviewed individually
(confirm no external use → decide). Build-verified after every change.

Removed (dead / redundant):
- `util.Math2` — redundant with `Math.multiplyExact` / `Math.floorDiv`.
- `util.Duplicates` — trivial Kotlin `groupingBy{}.eachCount()...` equivalent.
- `protobuf.Diff` — unused, only its own test.
- `protobuf.MessageFieldException` — `RuntimeException` thrown by nobody.
- `code.proto.FileOption` — dead leaf of the option family (no subclass/use).
- `base.RejectionType` — already removed by the maintainer.

Tightened:
- `base.IdType` — package-private (used by `Identifier` via package-private
  methods only).
- `io.IoPreconditions` — made `final` (its spec now uses `UtilityClassTest`).

Kept public (with reason):
- `string.MissingStringifierException` — thrown by `StringifierRegistry`,
  documented `@throws` on public `Stringify` API.
- `type.ApiOption` — used in production (same-package private fns); a
  meaningful public option-reading API.
- `type.NestedTypeName` — return type of public `Type.nestedSimpleName()`.
- `option.BaseOptionsProvider` — `@AutoService` SPI, `ServiceLoader`-loaded
  (package-private would break it at runtime; compiler wouldn't catch).
- `query.{RecordCriterion, IdParameter, CustomSubjectParameter,
  RecordSubjectParameter}` — all reachable through the public query DSL
  (return/param/type-argument of public query API).
- `code.proto.{AbstractOption, MessageOption}` — public superclasses of
  externally-used option types (`ColumnOption`/`EntityStateOption` etc.).
- `util.Predicates2`, `io.IoPreconditions` — cross-package `base` utilities;
  left public (no `@Internal`, per maintainer).

Separately, the maintainer converted `io.Ensure` to top-level Kotlin
functions and a missing-coverage test for `ensureFile` over an existing
file was added.

## Status
Type-level over-exposure review COMPLETE for the `base` module. Member-level
(~727 public members) remains for an IDE "weakest access" inspection pass.
