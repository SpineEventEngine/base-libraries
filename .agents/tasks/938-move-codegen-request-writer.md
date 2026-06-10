---
slug: 938-move-codegen-request-writer
branch: claude/busy-dirac-wwflbm
owner: claude
status: in-progress
started: 2026-06-10
---

## Goal

`io.spine.code.proto.CodeGeneratorRequestWriter` is removed from `base`
(it is protoc-plugin tooling, not runtime API), and the build is green.
Closes [#938](https://github.com/SpineEventEngine/base-libraries/issues/938)
together with the receiving change in `tool-base`.

## Context

- The class moves to the `tool-base` module of the ToolBase repository under
  `io.spine.tools.code.proto` (same-named branch there).
- The only consumers are the protoc-plugin entry points of the Compiler and
  ProtoTap; they migrate by switching the import once both PRs are published.
- `CodeGeneratorRequestParsingSpec.kt` and `CodeGeneratorRequestsJavaSpec.java`
  stay: they test `io.spine.type` parsing APIs which remain in `base`, and the
  Java spec still uses the `constructRequest` helper declared in the former.
- Removing public API is a breaking change: the snapshot version advances to
  the next multiple of 10.

## Plan

- [x] Remove `base/src/main/kotlin/io/spine/code/proto/CodeGeneratorRequestWriter.kt`.
- [x] Remove `base/src/test/kotlin/io/spine/code/proto/CodeGeneratorRequestWriterSpec.kt`.
- [x] Bump version `2.0.0-SNAPSHOT.404` -> `2.0.0-SNAPSHOT.410` (breaking).
- [ ] `./gradlew build` green; commit regenerated dependency reports if any.
  - Blocked in the sandbox: all Spine artifact repositories return 403 for
    the buildscript dependency `io.spine.tools:protobuf-setup-plugins`, so
    no Gradle build can run here at all. Verification is delegated to PR CI.
  - Repo-wide greps confirm no remaining references to the removed class;
    the surviving `CodeGeneratorRequest*` specs do not use it.
- [x] Push and open a draft PR; merge after the tool-base PR.

## Log

- 2026-06-10 — drafted; executing autonomously per issue #938.
- 2026-06-10 — removal committed and version bumped; sandbox cannot resolve
  Spine snapshot artifacts (403 on all repos), so the build runs on PR CI
  instead.
