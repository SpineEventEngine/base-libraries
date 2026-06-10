---
slug: move-message-file-to-core-jvm-compiler
branch: claude/cool-lamport-rnwzpu
owner: claude
status: in-progress
started: 2026-06-10
---

## Goal

Resolve [#941](https://github.com/SpineEventEngine/base-libraries/issues/941):
`base` no longer ships `io.spine.base.MessageFile`; the enum lives in
CoreJvmCompiler (`core-jvm-base`, package `io.spine.tools.core.jvm`), where its
only org-wide consumer (`CoreJvmCompilerSettings`) resides. `base` compiles and
its tests pass without the enum.

## Context

- The enum encodes a *code-generation* file-naming convention
  (`commands.proto`, `events.proto`, `rejections.proto`), so it belongs to
  the tooling chain, not the runtime `base` artifact.
- Strategy: **coordinated hard move** (no `@Deprecated` shim), following the
  precedent of `move-fs-dir-types-to-tool-base` (archived task, approved
  2026-06-04). Cross-repo move without `git mv` is accepted for the same
  reason.
- Internal usages inside `base` (overlooked by the issue text) must be
  refactored first:
  - `io.spine.code.proto.FileName.matches(MessageFile)` — private helper
    behind `isCommands()/isEvents()/isRejections()`.
  - `io.spine.code.proto.FieldDeclaration.isCommandsFile()` — uses
    `MessageFile.COMMANDS.test(...)`.
- `FileName.isCommands()/isEvents()/isRejections()` keep direct coverage in
  `FileNameSpec.kt`, so no coverage is lost by the refactoring.
- The CoreJvmCompiler side is tracked in that repo's task file of the same
  slug (branch `claude/cool-lamport-rnwzpu` there as well). The two PRs are
  independent: the new enum lives in a different package, and CoreJvmCompiler
  pins a published `spine-base` that still contains the old class until its
  `Base` dependency is bumped later.

## Plan

- [x] Refactor `FileName.java`: inline the three suffix constants, replace
      `matches(MessageFile)` with a private `hasSuffix(String)`.
- [x] Refactor `FieldDeclaration.isCommandsFile()` to
      `FileName.from(file).isCommands()`.
- [x] `git rm base/src/main/java/io/spine/base/MessageFile.java`.
- [x] `git rm base/src/test/java/io/spine/base/MessageFileTest.java` and
      `base/src/test/proto/spine/test/base/message_file_test_events.proto`
      (test ported to CoreJvmCompiler).
- [x] Update copyright headers of modified files to 2026.
- [x] Bump `version.gradle.kts` → `2.0.0-SNAPSHOT.410` (combined with PR #943); project version
      strings in `docs/dependencies/*` updated to match (no dependency
      changed, so the regenerated content differs only in those strings).
- [ ] Build `:base` and run its tests — **blocked locally**: the remote
      session's network policy returns 403 for the Spine artifact
      repositories (GitHub Packages, CloudRepo, Artifact Registry), so
      Gradle cannot resolve `io.spine.tools:protobuf-setup-plugins` and
      friends. Verification delegated to PR CI.
- [x] Commit, push, draft PR referencing #941.

## Log

- 2026-06-10 — drafted; executing (work authorized by the issue assignment).
- 2026-06-10 — sources done; local Gradle build impossible (403 from all
  Spine repos under the session network policy); relying on PR CI.
