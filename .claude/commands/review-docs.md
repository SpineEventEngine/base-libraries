---
description: Review documentation changes (KDoc/Javadoc and Markdown) against Spine documentation conventions.
argument-hint: "[base-ref | --staged | paths...]"
allowed-tools: Read, Grep, Glob, Bash(git diff:*), Bash(git log:*), Bash(git status:*), Bash(git rev-parse:*), Bash(git ls-files:*)
---

Follow the `review-docs` skill exactly:

- Skill: `.agents/skills/review-docs/SKILL.md`
- Scope / flags: $ARGUMENTS
  - Empty: review the current branch's diff against `master` (`git diff master...HEAD`).
  - `--staged`: review staged changes only (`git diff --staged`).
  - A base ref (e.g. `master`, `origin/master`, a commit SHA): review `git diff <ref>...HEAD`.
  - Explicit paths: limit the review to those paths in addition to the diff scope.
- The skill owns the procedure, the per-area checks (KDoc/Javadoc, Markdown,
  prose flow, terminology), and the output format (Must fix / Should fix /
  Nits + one-line verdict).
- Stay in scope: documentation only. If a code-quality issue surfaces,
  note it briefly as a Nit pointing at `/review` (or the `kotlin-review`
  agent) — do not expand the review.
- Read-only: do not edit files, do not run builds.
