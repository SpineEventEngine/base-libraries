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

Coordinated release wiring done:
- [x] `base` published to Maven Local at `2.0.0-SNAPSHOT.400` (without the
      moved classes).
- [x] tool-base's `io.spine.dependency.local.Base` bumped to
      `2.0.0-SNAPSHOT.400`; tool-base built successfully against it —
      confirms `base` no longer ships `AbstractDirectory`/
      `SourceCodeDirectory` and tool-base's own copies satisfy all consumers.

tool-base `2.0.0-SNAPSHOT.390` published to Maven Local **with the moved
classes** (verified: `io/spine/tools/fs/{AbstractDirectory,
SourceCodeDirectory}.class` present in the jar).

The mc-js consumer update belongs to the **mc-js** repo and is tracked
there — see its `.agents/tasks/relocate-source-code-directory-to-tool-base.md`
(committed + pushed on branch `relocate-source-code-directory-to-tool-base`).

Remaining (base/tool-base side — user's call):
- [x] tool-base own version bump for its PR version gate (user said they
      bumped it; `version.gradle.kts` still shows `.390` here).
- [x] Commits in `base-libraries` + `tool-base` (awaiting authorization).

## Goal

Relocate the two tool-exclusive directory types of `io.spine.code.fs`
— `AbstractDirectory` and `SourceCodeDirectory` — out of the published
`base` artifact (repo `base-libraries`) and into `tool-base`'s
`io.spine.tools.fs` package, where their only consumers live. The three
remaining types (`FsObject`, `AbstractFileName`, `AbstractSourceFile`)
stay in `base` because the **core** `io.spine.code.proto` type system
depends on them.

Success = `base` no longer ships `AbstractDirectory`/`SourceCodeDirectory`
and `tool-base` compiles against the new location. The one downstream
consumer outside `tool-base` is `mc-js`, updated in its own repo.

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
- [x] Delete `AbstractDirectory.java` + `SourceCodeDirectory.java`
      (`git rm`; cross-repo `git mv` isn't possible — origin noted here).
- [x] `io.spine.code.fs` package-info still accurate (stays, describing
      the 3 remaining types).
- [x] Moved-test cleanup: stripped `SourceCodeDirectory` stubs/tests from
      `FsObjectSpec.kt` (coverage ported to tool-base).
- [x] Built + published to Maven Local at `2.0.0-SNAPSHOT.400`.

### tool-base
- [x] Add `AbstractDirectory.java`, `SourceCodeDirectory.java` under
      `io.spine.tools.fs` with updated `package` + 2026 copyright header.
- [x] Update imports `io.spine.code.fs.AbstractDirectory` →
      `io.spine.tools.fs.AbstractDirectory` (same-package refs drop the
      import) in:
      `plugin-base`: `GeneratedSourceRoot`, `GeneratedSourceSet`;
      `tool-base`: `BuildRoot`, `DefaultPaths`, `DescriptorsDir`,
      `Generated`, `SourceDir`, `SourceRoot`, `Src`,
      `java/fs/DefaultJavaPaths`, `js/fs/DefaultJsPaths`,
      `proto/fs/Directory` (import order fixed where needed).
- [x] Update `io.spine.code.fs.SourceCodeDirectory` →
      `io.spine.tools.fs.SourceCodeDirectory` in:
      `tool-base`: `SourceDir`, `proto/fs/Directory`,
      `java/fs/FsTypesExts.kt`, `js/fs/FsTypesExts.kt`;
      test: `js/fs/FsTypesExtensionsSpec.kt`.
- [x] Ported `SourceCodeDirectory.resolve(...)` coverage →
      `tool-base/src/test/.../io/spine/tools/fs/SourceCodeDirectorySpec.kt`.
- [x] `io.spine.dependency.local.Base` bumped `2.0.0-SNAPSHOT.390` →
      `.400`; tool-base built successfully against the de-classed base.
- [ ] Bump tool-base's own `version.gradle.kts` (still `.390`) for PR gate.

### mc-js
Out of scope for this repo — tracked in the mc-js repo's own task note.

### Publish order
base-libraries → tool-base (→ mc-js, tracked separately). Each published
to Maven Local for downstream integration tests before release.

## Open questions

Both resolved — see **Decisions** above (A: hard move; mc-js: separate PR).
