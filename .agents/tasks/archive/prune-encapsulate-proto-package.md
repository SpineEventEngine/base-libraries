---
slug: prune-encapsulate-proto-package
branch: increse-coverage
owner: claude
status: done-pending-commit
started: 2026-06-04
---

## Goal

Act on the org-wide usage analysis of `io.spine.code.proto`: remove
production-dead types and shrink the published API surface for types not
used outside `base-libraries`. No type qualified for an `fs`-style move —
the package is runtime core (used by `io.spine.type`/`base`/`query` and by
core-jvm runtime), so nothing is relocatable.

## Part A — remove (production-dead, test-only)

- [x] `OneofDeclaration` + `OneofDeclarationTest` — referenced only by its
      own test anywhere in the org. Pure dead code.
- [x] `FieldContext` + `FieldContextKtSpec` — zero production use org-wide;
      exercised only by its own test and one **vestigial** line in
      validation's test.
      ⚠️ Downstream note: validation pins base `2.0.0-SNAPSHOT.391` (new
      `io.spine.dependency` layout). Its
      `validation/tests/runtime/src/test/kotlin/io/spine/validation/ValidateUtilitySpec.kt`
      has `import io.spine.code.proto.FieldContext` and
      `tester.setDefault(FieldContext::class.java, FieldContext.empty())`.
      `Validate.java` does NOT reference `FieldContext` anywhere, so the
      `setDefault` is dead leftover that `NullPointerTester` ignores. Fix on
      validation's next base bump = delete those two lines (pure cleanup, no
      production impact). Nothing breaks until that bump.

## Part B — encapsulate (not used outside base-libraries)

Applied the *safe* subset after a hierarchy/exposure check:

- [x] `FieldTypesProto` → **package-private** (used only inside
      `FieldTypes` method bodies).
- [x] `UnderscoredName` → **package-private** (referenced as a type only
      within the package; public `FieldName`/`FileName` implement it but no
      cross-package/external code references the interface type).
- [x] `LocationPath` → **`@Internal`** (used cross-package only by
      `io.spine.type`; `final`, no external use).
- [x] `PackageName` → **`@Internal`** (same; `final`, used by
      `io.spine.type`).

### Deliberately NOT changed (and why)
- `CamelCase`, `Linker` — **already** package-private.
- `AbstractOption`, `MessageOption`, `FileOption` — although not directly
  referenced outside base, they form the **public option-type hierarchy**
  (`Option`, `FieldOption`, `ColumnOption`, `EntityStateOption` are public
  API consumed by **core-jvm** runtime). Reducing the superclasses would be
  inaccurate and risks a public-subtype-of-reduced-supertype shape. Left
  public. (`FileOption` is genuinely unused — a future removal candidate if
  the option family symmetry is not a concern.)

## Verification

- `./gradlew :base:compileJava :base:compileTestJava :base:compileTestKotlin`
  → success.
- `./gradlew :base:test` → **887/887 pass**, 0 failed.

## Remaining (user's call)
- [ ] Bump `version.gradle.kts` (production API change) — base currently
      `2.0.0-SNAPSHOT.400`; needs an increment before publish.
- [ ] Commit (base-libraries commits handled by the maintainer).
- [ ] Follow-up in **validation**: adjust `ValidateUtilitySpec` when it
      moves to the new base (tracked there, not here).
