---
slug: 958-more-known-types-guard
branch: update-more-known-types-path
owner: claude
status: in-review
started: 2026-08-19
---

## Goal

`KnownTypes.Holder.extendWith` permits its only legitimate caller after the
latter moves to `io.spine.tools.proto.type.MoreKnownTypes`, and a test pins
the allowed name so a future rename fails here instead of in a consumer's
Gradle build.
Closes [#958](https://github.com/SpineEventEngine/base-libraries/issues/958).

## Context

- `MoreKnownTypes` moves from `io.spine.tools.type` to
  `io.spine.tools.proto.type` in `tool-base` as part of splitting the
  monolithic `tool-base` module (SpineEventEngine/tool-base#189).
- The guard is a runtime stack check, so nothing fails at compile time; the
  `SecurityException` surfaces when `protobuf-setup-plugins` calls
  `extendWith` during a consumer's build.
- Per the issue, the single updated name suffices given the release ordering
  (`base-libraries` first, then `tool-base`). `InvocationGuard.allowOnly`
  does already have a vararg overload if a migration window is wanted later.
- `KnownTypesSpec` only had the negative case (client code is rejected);
  nothing pinned the *allowed* name.

## Plan

- [x] Update the allowed FQN and the `@Internal` comment in `KnownTypes.java`.
- [x] Add a test double named `io.spine.tools.proto.type.MoreKnownTypes` in
      `base` test sources, and a positive `KnownTypesSpec` case asserting the
      guard admits it.
- [x] Fix the mangled `SecurityException` message in `InvocationGuard`
      (`"$callingClass.name"` interpolates the class then appends `.name`).
- [x] Version already bumped on this branch: `.440` -> `.441`.
- [x] Build green (`build` + `dokkaGenerate` on JDK 17); report the Base
      version carrying the fix on the issue.

## Log

- 2026-08-19 — plan drafted from issue #958.
- 2026-08-19 — guard name updated; `MoreKnownTypes` test double added under
  `base/src/test/kotlin/io/spine/tools/proto/type/`; verified the new test
  fails when the guard still names the old package.
- 2026-08-19 — `./gradlew build` and `dokkaGenerate` green; dependency
  reports regenerated (they were stale after the `.441` bump and the
  `config` update already on this branch).
- 2026-08-19 — pre-PR gate PASS: build + `dokkaGenerate` green;
  `spine-code-review`, `kotlin-engineer`, `review-docs` all approved.
  Their findings folded in: KDoc now records that the double must call
  `extendWith` directly, since the guard resolves the immediate frame.
- 2026-08-19 — PR opened:
  https://github.com/SpineEventEngine/base-libraries/pull/959
