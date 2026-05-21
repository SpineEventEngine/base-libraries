---
name: pre-pr
description: >
  Run the pre-PR checklist for this repo: apply the version gate only when
  the repository has a root `version.gradle.kts`, run the configured
  build/check command per `.agents/running-builds.md`, and invoke the
  configured reviewers (`kotlin-review`, `review-docs`, `dependency-audit`)
  against the branch diff. On success, write a sentinel file at
  `.git/pre-pr.ok` so the `gh pr create` hook can verify the checklist ran
  for the current HEAD. Use before opening a PR, or when CI rejected a
  branch and you want a fast local repro.
---

# Pre-PR checklist (repo-specific)

You are the pre-PR gate for this repository. You compose the existing
reviewers and the documented repository rules into a single pass that must
succeed before a pull request is opened.

This skill supports both versioned Gradle Build Tools projects and repositories
that intentionally do not have `version.gradle.kts` (for example, shared
configuration repositories). Do not create `version.gradle.kts` just to satisfy
this checklist. When the file is absent from the project root, the version-bump
check is **not applicable**.

The authoritative standards live in `.agents/`:

- `.agents/version-policy.md` — applies only when the repository has a root
  `version.gradle.kts`.
- `.agents/running-builds.md` — which build/check command to run based on what
  changed. It may be Gradle or another repository-specific command.
- `.agents/safety-rules.md` and `.agents/advanced-safety-rules.md` — hard
  constraints checked by the reviewers.
- The reviewer skills/agents themselves: `kotlin-review` (Claude agent),
  `review-docs` (skill + Claude agent), `dependency-audit` (Claude agent).

## Procedure

Execute the steps in order. If a step fails, stop, write a `FAIL` sentinel
(see step 6), and report the failure — do not run the remaining steps.

### 1. Determine scope and repository capabilities

- Base ref: `master` unless the user provides a different one.
- Diff command: `git diff <base>...HEAD --name-only` for the file list,
  `git diff <base>...HEAD --stat` for the summary.
- Repository root: `git rev-parse --show-toplevel`.
- Version gate:
  - Check only the repository-root `version.gradle.kts`.
  - If `version.gradle.kts` is absent at both `<base>` and `HEAD`, record the
    version check as `N/A` and continue. Do not ask the user to run
    `/bump-version`.
  - If `version.gradle.kts` exists at `HEAD`, enforce the version check in
    step 2.
  - If `version.gradle.kts` exists at `<base>` but is missing at `HEAD`, fail
    unless the user explicitly asked to migrate the repository away from
    Gradle Build Tools versioning.
- Classify the changes:
  - **proto** — any `*.proto` file changed.
  - **code** — any `*.kt`, `*.kts`, or `*.java` file changed.
  - **docs** — any `*.md` file or doc-only edits inside sources changed.
  - **deps** — any file under `buildSrc/src/main/kotlin/io/spine/dependency/`
    changed.

### 2. Version-bump check

- If the version gate is `N/A`, skip this step with note:
  "`version.gradle.kts` is absent; this repository is not a versioned Gradle
  Build Tools project."
- Otherwise, read `version.gradle.kts` at `HEAD` and, when present, at
  `<base>`.
- Confirm the version string is strictly greater (semver + Spine snapshot
  rules — see `.agents/version-policy.md`) when both sides have the file.
- If the file is newly introduced at `HEAD`, report the introduced version and
  continue.
- If unchanged or decreased, stop with a Must-fix: "Run `/bump-version`."

### 3. Build or check

Pick the target per `.agents/running-builds.md`:

- **proto** changed → `./gradlew clean build`
- Else **code** changed → `./gradlew build`
- Else **docs**-only → `./gradlew dokka` (tests not required)

If the repository does not have `./gradlew`, do not fail solely because Gradle
is unavailable. Read `.agents/running-builds.md` for the repository-specific
non-Gradle command that matches the change type, and run that instead. If no
build/check command is documented for the change type, record `build=skipped`
with the reason and continue.

Run the chosen command. Surface the first failing module/task/check. On
failure, stop and write a `FAIL` sentinel.

### 4. Reviewers (run in parallel)

Dispatch the relevant reviewers concurrently and collect their verdicts:

- Always: `kotlin-review` (if **code** changed) and `review-docs` (if
  **docs** or KDoc changed).
- If **deps** changed: `dependency-audit`.

Pass each reviewer the base ref, changed-file list, build/check result, and
version-check result. When the version check is `N/A`, say explicitly:
"This repository has no root `version.gradle.kts`; a version bump is not
applicable and must not be reported as missing."

Each reviewer is read-only and emits a Must-fix / Should-fix / Nits
report plus a one-line verdict (`APPROVE`, `APPROVE WITH CHANGES`, or
`REQUEST CHANGES`).

### 5. Aggregate

- Overall **PASS** when:
  - Version check passed or was `N/A`,
  - Build succeeded,
  - Every dispatched reviewer returned `APPROVE` or `APPROVE WITH CHANGES`
    *and* no Must-fix items remain unaddressed in this session.
- Otherwise **FAIL**.

### 6. Sentinel

Write `.git/pre-pr.ok` at the repo root (NOT under `.claude/` — the
sentinel must travel with the local clone, not be checked in). Format:

```
head=<full HEAD SHA>
branch=<current branch>
status=PASS|FAIL
timestamp=<ISO-8601 UTC>
build=<the build/check command that was run, or "skipped">
reviewers=<comma-separated reviewer names that were invoked>
version=<old->new, introduced:<new>, or "not-applicable">
```

The `gh pr create` hook (`.agents/scripts/pre-pr-gate.sh`) checks this
file's `head=` and `status=` fields. Extra fields are allowed. The sentinel is
invalidated automatically when HEAD advances — the hook compares the recorded
`head=` against the current HEAD SHA.

## Output format

Report in this shape:

```
## Pre-PR checklist (<branch> vs <base>)

| Check         | Status | Notes                                  |
|---------------|--------|----------------------------------------|
| Version check | …      | <old> → <new>, introduced, or N/A      |
| Build/check   | …      | <command>                              |
| kotlin-review | …      | <verdict + count of Must/Should>       |
| review-docs   | …      | <verdict + count of Must/Should>       |
| dep audit     | …      | <verdict + count of Must/Should>       |

**Overall: PASS|FAIL**
Sentinel: .git/pre-pr.ok (status=PASS|FAIL, head=<SHA>)
```

On `PASS`, end with: "You can now run `gh pr create`."
On `FAIL`, end with the specific blocker and the next action.

## Notes

- This skill must NOT create the PR itself. It only gates whether the
  workspace is ready.
- This skill must NOT create `version.gradle.kts`. Repositories without a root
  `version.gradle.kts` are valid; their version check is `N/A`.
- The sentinel lives under `.git/` (untracked by definition) so it is
  per-clone and never committed.
- Each reviewer remains the source of truth for its own checks; this
  skill does not duplicate their rules — it only orchestrates and
  aggregates.
