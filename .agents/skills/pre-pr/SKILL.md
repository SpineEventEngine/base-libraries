---
name: pre-pr
description: >
  Run the pre-PR checklist for this repo: apply the version gate only when
  the repository has a root `version.gradle.kts`, run a scope-dependent
  build/check command per `.agents/running-builds.md` (docs-only → `dokka`;
  code/deps → `build`; proto → `clean build`; no documented command → skipped),
  and invoke the relevant reviewers (`kotlin-review`, `review-docs`,
  `dependency-audit`,
  `check-links`) against the branch diff. On success, write a sentinel file at
  `.git/pre-pr.ok` so the `gh pr create` hook can verify the checklist ran
  for the current HEAD. Use before opening a PR, or when CI rejected a
  branch and you want a fast local repro.
---

# Pre-PR checklist (repo-specific)

You are the pre-PR gate for this repository. You compose the existing
reviewers and the documented repository rules into a single pass that must
succeed before a pull request is opened.

This skill supports both versioned Gradle Build Tools projects and repositories
that intentionally do not have `version.gradle.kts`. Do not create
`version.gradle.kts` just to satisfy this checklist. When the file is absent
from the project root, the version-bump check is **not applicable**.

The authoritative standards live in `.agents/`:

- `.agents/version-policy.md` — applies only when the repository has a root
  `version.gradle.kts`.
- `.agents/running-builds.md` — which build/check command to run.
- `.agents/safety-rules.md` and `.agents/advanced-safety-rules.md` — hard
  constraints checked by the reviewers.

## Procedure

Run steps 1–4 fully before aggregating. Collect all findings; do not stop at
the first failure.

### 1. Determine scope and repository capabilities

- Base ref: `master` unless the user provides a different one.
- Changed files: `git diff <base>...HEAD --name-only`
- Repository root: `git rev-parse --show-toplevel`
- Repository kind: detect the `config` repository by scanning `git remote -v`
  for any URL matching `[:/]SpineEventEngine/config(\.git)?$`.
- Filter changed files using `AGENTS.md § Code review`:
  - In **`config` itself**, skip only `gradlew` and `gradlew.bat`; every other
    config-distributed path is owned by this repo and stays in scope.
  - In any **consumer repo**, remove the full config-distributed skip list. A
    PR that contains *only* config-distributed files needs no build, no
    reviewers, and should PASS immediately — skip to step 6 with
    `build=skipped`, `build_status=skipped`, `reviewers=none`,
    `version=not-applicable`.
- Version gate: check only the repository-root `version.gradle.kts`.
  - Absent at both sides → `not-applicable`, continue.
  - Present at `HEAD` → enforce in step 2.
  - Present at `<base>` but missing at `HEAD` → fail unless the user
    explicitly asked to migrate away from Gradle Build Tools versioning.
- Hugo site: detect a site only when `docs/` or `site/` contains one of
  `hugo.toml`, `hugo.yaml`, `config/hugo.toml`, `config/hugo.yaml`,
  `config/_default/hugo.toml`, or `config/_default/hugo.yaml`.
- Classify changes:
  - **proto** — any `*.proto` changed
  - **code** — any `*.kt`, `*.kts`, or `*.java` changed
  - **docs** — any `*.md` or doc-only source edits changed
  - **deps** — any file under `buildSrc/src/main/kotlin/io/spine/dependency/` changed
  - **site** — a Hugo site exists and any file under `docs/**` or `lychee.toml`
    changed (triggers Hugo link check; pure `README.md` or KDoc-only changes do
    *not* count). If `lychee.toml` changes but no Hugo site exists, keep
    `site=false` and note that `check-links` is not applicable if the skipped
    reviewer needs explanation.

### 2. Version-bump check

- Skip when version gate is `not-applicable`.
- Read `version.gradle.kts` at `HEAD`. Read `<base>` only if the file exists
  there; if it does not, the file is newly introduced — record the introduced
  version and continue.
- When both sides have the file: if the version is not strictly greater (semver
  + Spine snapshot rules in `.agents/version-policy.md`): if
  `.agents/skills/bump-version/` exists, **auto-fix immediately** by running
  the `bump-version` skill without asking; otherwise record a Must-fix and continue.
  Re-read the file after the fix. If the version is still not strictly greater,
  record a Must-fix and continue. If the auto-fix succeeded, recompute the
  changed-file list (`git diff <base>...HEAD --name-only`) before proceeding to
  Step 3 — the bump commit adds `version.gradle.kts` to the diff.

### 3. Build or check

Pick the target per `.agents/running-builds.md`:

- **proto** changed → `./gradlew clean build`
- Else **code** changed → `./gradlew build`
- Else **docs**-only → `./gradlew dokka`

If `./gradlew` is absent, read `.agents/running-builds.md` for the
repository-specific command. If that file is also absent, or if none is
documented for the change type, record `build_status=skipped` with the
reason and continue.

Run the chosen command. On failure, record the first failing task and
continue to step 4 — do not abort. Pass `build_status=FAIL` in the context
given to reviewers so they can discount false positives from non-compiling
code.

### 4. Reviewers

Run every relevant reviewer and collect all verdicts before aggregating. In a
single Codex session, run the reviewer skills one by one and batch independent
search/read commands inside each reviewer. If multi-agent tools are available,
parallel reviewer dispatch is optional but not required.

Before running a reviewer, check that the skill directory exists under
`.agents/skills/`; if a skill is absent, skip it with a note "not applicable
for this repo" rather than failing.

- **code** changed → `kotlin-review`
- **docs** or KDoc changed → `review-docs`
- **deps** changed → `dependency-audit`
- **site** changed → `check-links` (unless the sentinel short-circuit below
  applies)

**`check-links` sentinel short-circuit.** Read `.git/check-links.ok` (if
present). If `head=` equals the current **full** HEAD SHA and `status=PASS`, skip
the link check and record `APPROVE` with note "cached from `.git/check-links.ok`"
(caching its ~30 s rebuild+serve cycle; the result is deterministic for a given
HEAD). Otherwise run `check-links` normally.

Pass each reviewer: base ref, changed-file list, build result, version result.
When the version check is `not-applicable`, say so explicitly so reviewers don't flag a
missing version bump.

**Auto-fix policy for reviewer findings:**

- Findings from `kotlin-review`, `review-docs`, or `dependency-audit` → record
  as Must-fix or Should-fix; do **not** auto-apply. Surface them and wait for
  user action.
- If a reviewer reports a missing version bump after Step 2 already ran, the
  auto-fix did not take — record a Must-fix and do not silently re-apply.
- `dependency-audit` reports a **version rollback** → do **not** auto-fix.
  Surface it as a Must-fix and wait for user confirmation, because a rollback
  can be intentional.

### 5. Aggregate

- **PASS**: version check passed or `not-applicable`, build succeeded or
  `build_status=skipped` (no documented command for the change type), every
  reviewer returned `APPROVE` or `APPROVE WITH CHANGES`, and no unaddressed
  Must-fix items remain.
- **FAIL**: anything else.

### 6. Sentinel

Write `.git/pre-pr.ok` at the repo root (never under `.claude/`). The `gh pr
create` hook (`.agents/scripts/pre-pr-gate.sh`) checks `head=` and `status=`;
field names in this block are part of that contract.

```
head=<full HEAD SHA>
branch=<current branch>
status=PASS|FAIL
timestamp=<ISO-8601 UTC>
build=<command run, or "skipped">
build_status=PASS|FAIL|skipped
reviewers=<comma-separated names invoked>
version=<old->new, introduced:<new>, or "not-applicable">
```

## Output format

**On PASS** — single line:

```
Pre-PR: PASS (<branch> vs <base>) — ready to `gh pr create`.
```

**On FAIL** — header line, then only the items that need attention, each
prefixed with the source reviewer or check:

```
Pre-PR: FAIL (<branch> vs <base>)

Must fix:
- [kotlin-review] <item>
- [review-docs] <item>

Should fix:
- [dependency-audit] <item>
```

Report nothing about checks that passed. If auto-fixes were applied, list
them in one line before the verdict: `Auto-fixed: <comma-separated list>.`

## Notes

- This skill must NOT create the PR itself.
- This skill must NOT create `version.gradle.kts`.
- The sentinel lives under `.git/` — per-clone, never committed.
- Each reviewer is the source of truth for its own checks; this skill only
  orchestrates and aggregates.
- This skill may auto-fix a missing version bump by running the `bump-version` skill;
  all other fixes require explicit user confirmation.
