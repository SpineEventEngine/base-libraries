---
description: Run the applicable pre-PR checklist (version gate, build/check, reviewers) and write a sentinel so `gh pr create` is unblocked.
argument-hint: "[base-ref]"
allowed-tools: Read, Write, Grep, Glob, Agent, Bash
---

Follow the `pre-pr` skill exactly:

- Skill: `.agents/skills/pre-pr/SKILL.md`
- Base ref: $ARGUMENTS (treat empty as `master`).
- Detect whether the repository-root `version.gradle.kts` exists. If it is
  absent at both the base ref and `HEAD`, the version check is `N/A`; do not
  create the file and do not ask for `/bump-version`.
- Run the build/check command selected by the skill and
  `.agents/running-builds.md`. The command may be Gradle or non-Gradle.
- Dispatch the reviewers as Claude subagents in parallel — send a single
  message with multiple Agent tool uses:
  - `kotlin-review` when `.kt|.kts|.java` files changed.
  - `review-docs` when `.md` files or KDoc inside sources changed.
  - `dependency-audit` when any file under
    `buildSrc/src/main/kotlin/io/spine/dependency/` changed.
- Pass the version-check status to reviewers. If it is `N/A`, tell them:
  "This repository has no root `version.gradle.kts`; a version bump is not
  applicable and must not be reported as missing."
- Each reviewer is read-only; do not pass it edit tools.
- On any reviewer returning `REQUEST CHANGES`, treat the overall result
  as `FAIL` and stop before writing the sentinel as `PASS`.
- Sentinel location: `$(git rev-parse --show-toplevel)/.git/pre-pr.ok`,
  format per the skill (`head=`, `branch=`, `status=`, `timestamp=`,
  `build=`, `reviewers=`, `version=`). Use `git rev-parse HEAD` for the
  SHA and `date -u +%Y-%m-%dT%H:%M:%SZ` for the timestamp.
- Do NOT run `gh pr create`. That is the user's next step.
