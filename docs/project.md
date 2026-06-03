# Project: base-libraries

## Overview

`base-libraries` is a foundational JVM repository in the Spine SDK organisation.
It hosts the common data types, annotations, environment helpers, and
parsing/serialization utilities that the rest of the Spine SDK (notably
[`core-jvm`](https://github.com/SpineEventEngine/core-java)) depends on. The
artifacts published from this repo sit at the bottom of the Spine dependency
graph, so changes here ripple into most other Spine projects.

## Architecture

Role: **library** (multi-module Gradle build) publishing the following Maven
artifacts under the `io.spine` group:

- `annotations` — annotation types used across the Spine SDK.
- `base` — common data types and utilities. Not consumed directly by
  end users; re-exposed as an `api` dependency by `spine-client` and
  `spine-server` in `core-jvm`.
- `environment` — runtime environment detection helpers.
- `format` — parsers for YAML, JSON, binary Protobuf, and Protobuf JSON;
  used internally by Spine SDK components.

Key constraints:

- Public API stability matters: downstream Spine repos pin to versions
  published from here, so removals and signature changes are breaking.
- No analytics, telemetry, reflection, or unsafe code (see
  `.agents/guidelines/safety-rules.md`).
- Versioning follows the Spine SDK policy (`.agents/guidelines/version-policy.md`);
  CI's `Version Guard` rejects branches that reuse a published version.
- Dependency declarations live under
  `buildSrc/src/main/kotlin/io/spine/dependency/` and are audited by the
  `dependency-audit` skill.

Read [`.agents/jvm-project.md`](jvm-project.md) for build stack, coding
style, tests, and versioning.
