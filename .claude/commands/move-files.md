---
description: Move or rename files/directories, updating all references and build metadata.
argument-hint: "<source> <destination>"
allowed-tools: Read, Edit, Bash(git mv:*), Bash(git status:*), Bash(git ls-files:*), Grep, Glob
---

Follow the `move-files` skill exactly:

- Skill: `.agents/skills/move-files/SKILL.md`
- Operation: $ARGUMENTS
- Preflight (run `git status --short`, classify scope) -> Search for all old identifiers -> Move with `git mv` -> Repair references (imports, build metadata, docs) -> Verify.
- Report: Moved[], UpdatedRefs[], Verification[], Risks[].
