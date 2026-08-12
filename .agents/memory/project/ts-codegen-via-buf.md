---
name: ts-codegen-via-buf
description: TypeScript codegen will likely use Buf (protobuf-es), not google-protobuf protoc — keep proto option docs implementation-neutral.
metadata:
  type: project
  since: 2026-08-11
---

TypeScript code generation for the Spine SDK is likely to be built on
Buf (`protobuf-es`) rather than the `google-protobuf` runtime used by
the legacy JS stack (`web/client-js`, `mc-js`).

**Why:** Stated by the project lead while reviewing the `ts_type`
property docs of `IsOption`/`EveryIsOption` (branch
`more-language-types`, 2026-08-11): the docs must not restrict
implementation details such as "extends the `Message` type from the
`google-protobuf` module", because the TS toolchain is not settled on
`google-protobuf`.

**How to apply:** When documenting or implementing TypeScript-facing
options in `options.proto` (or elsewhere in the SDK), stay neutral
about the generation toolchain and runtime base types until the TS
codegen actually lands. Do not infer TypeScript contracts from what
`web/client-js` currently depends on.
