---
slug: add-ts-type-option
branch: more-language-types
owner: claude
status: in-review
started: 2026-08-11
---

## Goal

`IsOption` and `EveryIsOption` in `base/src/main/proto/spine/options.proto`
gain a `ts_type` property naming the TypeScript interface for the annotated
message type(s), mirroring the existing `java_type` contract.

## Context

- Requested by the user on the `more-language-types` branch, which is
  already version-bumped (`2.0.0-SNAPSHOT.440`).
- No consumer of `ts_type` exists yet anywhere in the SpineEventEngine
  organisation; this repo introduces the property first. Codegen support
  arrives later in the compiler/model-compiler repos.
- Spine's JS/TS client stack (`web/client-js`) is built on the
  `google-protobuf` runtime, so a generated marker interface extends
  `Message` from that module — the parallel of `com.google.protobuf.Message`
  in the `java_type` docs.

## Plan

- [x] Add `ts_type = 2` to `IsOption` with docs mirroring `java_type`.
- [x] Add `ts_type = 3` to `EveryIsOption`, including the `generate`
      interplay paragraphs.
- [x] Extend the `(is)` and `(every_is)` extension-field docs with
      "When targeting TypeScript…" sentences.
- [x] Mention `ts_type` in the message-level docs of both option types.
- [x] Build (`proto` change → `clean build` per `running-builds.md`).

## Log

- 2026-08-11 — drafted; executing.
- 2026-08-11 — `./gradlew clean build` green; `review-docs` approved the
  diff (nits only). Discussing the "not nested into a namespace" wording
  with the user; docs may still be adjusted before commit.
- 2026-08-11 — per user: dropped the "generated interface extends
  `Message` from `google-protobuf`" paragraph — TS codegen will likely
  use Buf, so docs stay implementation-neutral (see team memory
  `ts-codegen-via-buf`). Rebuilt: green.
