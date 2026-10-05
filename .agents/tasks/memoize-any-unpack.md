---
slug: memoize-any-unpack
branch: memoize-any-unpack
owner: claude
status: in-review
started: 2026-10-05
---

## Goal

`AnyPacker.unpack(Any, Class<T>)` unpacks through protobuf's
`Any.unpackSameTypeAs(T exemplar)`, passing the default instance it already obtains
via `Messages.getDefaultInstance(cls)`. Repeated unpacking of one `Any` instance then
parses once, because `Any` remembers the unpacked message.

## Context

- Today the method parses `any.getValue()` on every call. It avoids `Any.unpack(Class)`
  because of its reflective lookup of the default instance, and thereby also bypasses
  the per-instance memo of `Any` (`cachedUnpackValue`).
- Downstream: `SpecScanner.MemoizingUnpacker` in `core-jvm` keeps an unbounded
  `Map<Any, S>` only to unpack a record's state once per several columns. It can be
  deleted once this change is published.
- Decisions taken with the maintainer on 2026-10-05:
  - No type check of our own. Protobuf's refusal is wrapped with
    `new UnexpectedTypeException(e)`. The private `checkType(..)` is removed.
  - The type URL prefix is no longer compared; protobuf matches by the type name.
  - For the typed overload, an empty or slash-less type URL now gives
    `UnexpectedTypeException` instead of `IllegalArgumentException`. `unpack(Any)`
    still throws `IllegalArgumentException`.
  - An `Any` that remembers a message of another Java class for the same Protobuf
    type (e.g. `DynamicMessage`) is refused by protobuf, and the caller gets
    `UnexpectedTypeException`. No Spine code on disk does this.
  - Identity and retention are accepted and documented in the class Javadoc.
- `UnexpectedTypeException(TypeUrl, TypeUrl)` loses its only caller. It stays as
  public API.

## Plan

- [x] Create the branch from `origin/master`.
- [x] Bump the version: `2.0.0-SNAPSHOT.451` -> `.452`.
- [x] Add the tests to `AnyPackerSpec.kt`; run them against the unchanged `AnyPacker`.
      Exactly the tests pinning the new behavior must fail.
- [x] Change `AnyPacker.unpack(Any, Class)`; all tests green.
- [x] Rewrite the documentation: `AnyPacker` (class and method),
      `UnexpectedTypeException`, `AnyExts.kt`.
- [x] `./gradlew build dokkaGenerate` on JDK 17; review the diff.
- [ ] Commit (awaiting authorization): the version bump, the change, then
      the regenerated dependency reports.

## Log

- 2026-10-05 — plan approved after two revisions of the type-check design;
  branch `memoize-any-unpack` created from `origin/master` (`887110901e`).
- 2026-10-05 — ten tests added to `AnyPackerSpec`. Against the unchanged `AnyPacker`
  five of them failed, as expected: same instance on a repeated unpack (two tests),
  a prefix-only difference, an empty type URL, and an `Any` remembering
  a `DynamicMessage`. The other seven tests of the suite passed.
- 2026-10-05 — `unpack(Any, Class)` switched to `Any.unpackSameTypeAs()`;
  `checkType(..)` removed. `AnyPackerSpec` (12 tests), `AnyPackerTest` (19) and
  `AnyExtensionsSpec` (4) are green.
- 2026-10-05 — documentation rewritten. The body of protobuf-kotlin's `unpack()`
  confirmed in its 4.36.0 sources: it calls `Any.unpack(Class)`.
- 2026-10-05 — `./gradlew build dokkaGenerate` green on JDK 17: `base` 1057 tests,
  `environment` 51, `format` 26; ErrorProne, PMD, Checkstyle and detekt passed.
  Dependency reports regenerated for `.452`. Nothing is committed.
- 2026-10-05 — found while checking the generated docs: Dokka renders no `@implNote`
  block of this repo, in either publication. The memoization section of `AnyPacker`
  is rendered; its implementation note is visible only in sources and IDEs.
  Not addressed here.
- 2026-10-05 — not verified: `core-jvm` was only read, not built or tested against
  this version; nothing was benchmarked (the repository has no JMH setup).
