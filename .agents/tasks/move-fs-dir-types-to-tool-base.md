---
slug: move-fs-dir-types-to-tool-base
branch: increse-coverage
owner: claude
status: in-progress
started: 2026-06-04
---

## Decisions (approved 2026-06-04)

- **Deprecation:** Strategy **A — coordinated hard move** (no `@Deprecated`
  shim in base).
- **mc-js:** handled in a **separate PR** in the `mc-js` repo (out of scope
  here).
- Cross-repo move without `git mv` is accepted.

## Status

Source move + import rewrites done and verified locally:
- base-libraries: `:base:test --tests io.spine.code.fs.*` → 8/8 pass.
- tool-base: `:tool-base:compileJava :tool-base:compileTestKotlin
  :plugin-base:compileJava` succeed; moved/extension fs tests → 3/3 pass.

Remaining (not done — user's call):
- [ ] Version bump in both repos (PR version gate) via `bump-version`.
- [ ] Coordinated release: republish base without the classes, then bump
      tool-base's `io.spine.dependency.local.Base` to that version
      (compile-independent; release-time only).
- [ ] Separate `mc-js` PR (import rewrite — see list below).
- [ ] Commit (awaiting user authorization).

## Goal

Relocate the two tool-exclusive directory types of `io.spine.code.fs`
— `AbstractDirectory` and `SourceCodeDirectory` — out of the published
`base` artifact (repo `base-libraries`) and into `tool-base`'s
`io.spine.tools.fs` package, where their only consumers live. The three
remaining types (`FsObject`, `AbstractFileName`, `AbstractSourceFile`)
stay in `base` because the **core** `io.spine.code.proto` type system
depends on them.

Success = `base` no longer ships `AbstractDirectory`/`SourceCodeDirectory`;
`tool-base` and `mc-js` compile against the new location; no other org
repo is affected.

## Context — usage analysis (verified 2026-06-04)

Org-wide consumers of `io.spine.code.fs` (GitHub code search + local
checkouts of `base-libraries`/`tool-base` + clone-and-grep of 12 tool
repos — 3,132 source files scanned, all clean):

| Type | base core | tool-base | mc-js | Disposition |
|------|:--:|:--:|:--:|---|
| `FsObject`            | base class only | — | — | **stays in base** |
| `AbstractFileName<F>` | `proto.FileName` | java/js/dart `FileName` | — | **stays in base** (proto.FileName used by `Type`, `MessageFile`, `RejectionType`, `SimpleClassName`) |
| `AbstractSourceFile`  | `proto.SourceFile` | `java.fs.SourceFile`, `FileWithImports` | — | **stays in base** (proto.SourceFile used by core `FileSet`) |
| `AbstractDirectory`   | — | 8 files | — | **MOVE** |
| `SourceCodeDirectory` | — | `SourceDir`, `proto.fs.Directory`, `FsTypesExts` | 9 files | **MOVE** |

Repos with **zero** references (do NOT need changes): mc-java,
model-compiler, compiler, core-jvm-compiler, ProtoTap, validation,
mc-dart, dart, bootstrap, doc-tools, dokka-tools, Chords-Gradle-plugin.

Dependency direction is favourable:
`plugin-base` → `tool-base` → `Base` (published spine-base). After the
move both classes still `extends FsObject` (remaining in base); `mc-js`
already depends on `tool-base`.

Note: `SourceCodeDirectory.resolve(AbstractSourceFile)` references
`AbstractSourceFile`, which stays in base — fine, tool-base sees it.
`SourceCodeDirectory`'s current header comment ("Exposed for `tool-base`")
confirms the SDK-internal intent of these types.

## Target location

`tool-base/tool-base/src/main/java/io/spine/tools/fs/`
  - `AbstractDirectory.java`  → `package io.spine.tools.fs;`
  - `SourceCodeDirectory.java` → `package io.spine.tools.fs;`

The package already exists and is the natural home (`SourceDir`,
`DefaultPaths`, `BuildRoot`, etc. all live there and already extend these
two types). The `@Immutable` annotations and `@SuppressWarnings` carry over;
drop the now-stale "Exposed for `tool-base`" comment on `SourceCodeDirectory`.

## Deprecation strategy — DECISION REQUIRED

These are public classes in a Maven-Central-published artifact. Two paths:

- **A. Coordinated hard move (recommended).** Delete from `base`, add to
  `tool-base`, update `tool-base` + `mc-js` imports in lockstep, publish
  in dependency order. Justified because the verified consumer set is
  entirely SDK-internal and the types were explicitly "exposed for
  tool-base". Cleanest; avoids a duplicate parallel hierarchy.
- **B. Deprecate-then-remove (conservative).** Keep `@Deprecated`
  copies in `base` for one release while the new ones live in tool-base;
  remove later. Cost: temporary class duplication and two parallel
  `AbstractDirectory` hierarchies — awkward because `SourceDir`/`Directory`
  can only extend one. Not recommended unless external subclassers are a
  concern.

## Plan (no code until strategy approved)

### base-libraries
- [ ] `git mv` `AbstractDirectory.java` + `SourceCodeDirectory.java` out
      (or delete, per strategy) — `git mv` cross-repo isn't possible, so
      this is delete-in-base + add-in-tool-base; preserve history via the
      tool-base commit message referencing the origin.
- [ ] Confirm `io.spine.code.fs` package-info still accurate (it stays,
      describing the 3 remaining types).
- [ ] Bump `version.gradle.kts` (version gate).
- [ ] `./gradlew build` — base must still compile (only proto.* use the
      remaining fs types).

### tool-base
- [ ] Add `AbstractDirectory.java`, `SourceCodeDirectory.java` under
      `io.spine.tools.fs` with updated `package` + copyright header.
- [ ] Update imports `io.spine.code.fs.AbstractDirectory` →
      `io.spine.tools.fs.AbstractDirectory` (same-package refs can drop
      the import) in:
      `plugin-base`: `GeneratedSourceRoot`, `GeneratedSourceSet`;
      `tool-base`: `BuildRoot`, `DefaultPaths`, `DescriptorsDir`,
      `Generated`, `SourceDir`, `SourceRoot`, `Src`,
      `java/fs/DefaultJavaPaths`, `js/fs/DefaultJsPaths`,
      `proto/fs/Directory`.
- [ ] Update `io.spine.code.fs.SourceCodeDirectory` →
      `io.spine.tools.fs.SourceCodeDirectory` in:
      `tool-base`: `SourceDir`, `proto/fs/Directory`,
      `java/fs/FsTypesExts.kt`, `js/fs/FsTypesExts.kt`;
      test: `js/fs/FsTypesExtensionsSpec.kt`.
- [ ] Point tool-base at the new base snapshot (version + publishToMavenLocal).
- [ ] `./gradlew build`.

### mc-js
- [ ] Update `io.spine.code.fs.SourceCodeDirectory` →
      `io.spine.tools.fs.SourceCodeDirectory` in:
      `code/index/CreateParsers`, `code/index/GenerateIndexFile`,
      `code/step/AppendTypeUrlGetter`, `code/step/CodeGenStep`,
      `code/step/CompiledProtoBelongsToModule`, `fs/FileWriter`;
      tests: `code/given/TestCodeGenStep`,
      `code/index/GenerateIndexFileTest`, `code/step/CodeGenStepTest`.
- [ ] Bump tool-base dependency; `./gradlew build`.

### Publish order
base-libraries → tool-base → mc-js (each to Maven Local for downstream
integration tests before release).

## Open questions
1. Deprecation strategy A vs B (above).
2. Should `mc-js` changes be in scope of this task, or tracked separately
   (it's a different repo with its own PR/release cycle)?
