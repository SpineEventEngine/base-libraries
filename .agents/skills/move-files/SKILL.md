---
name: move-files
description: >
  Move or rename any files/directories in a repo: preserve history, update all
  references and build metadata, verify no stale paths remain.
---

# Move Files

## Workflow

1. Preflight.
   - Run `git status --short`.
   - Map each `source -> destination`.
   - Classify scope: simple same-module moves stay targeted; package, module, or
     cross-module moves need broader inspection.
   - Ask before ambiguous mappings, destination conflicts, or unclear semantic
     package/module changes.

2. Search before moving.
   - Search all old identifiers: paths, names, resource refs, doc links.
   - For Gradle/module/source-set moves, check `settings.gradle.kts`,
     `build.gradle.kts`, and `buildSrc`.
   - For Kotlin/Java, update package declarations only when package intent
     changes.

3. Move safely.
   - Prefer `git mv` for tracked files in the repo.
   - Use filesystem moves only for untracked/generated/out-of-git files.
   - Create parent directories first.
   - For case-only renames, move through a temporary name.

4. Repair references.
   - Update all references: imports, build metadata, docs, resources, and scripts.
   - Start search scope narrow: affected directory, then module, then repo-wide.
   - Prefer precise edits; avoid broad replacements on generic names.

5. Verify.
   - Re-run targeted searches for old tokens.
   - Run `git status --short` and confirm the delta matches the move.
   - Run focused validation for moved files, or state what could not run.

## Repo Notes

Follow `.agents/project-structure-expectations.md` for module/source-set/test moves.

## Report

Return: `Moved[]`, `UpdatedRefs[]`, `Verification[]`, `Risks[]`.
