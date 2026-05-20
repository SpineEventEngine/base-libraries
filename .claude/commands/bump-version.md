---
description: Bump the project version in version.gradle.kts per Spine SDK versioning policy.
argument-hint: "[snapshot|minor|major]"
allowed-tools: Read, Edit, Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(./gradlew:*)
---

Follow the `bump-version` skill exactly:

- Skill: `.agents/skills/bump-version/SKILL.md`
- Read the skill first; it owns the policy (snapshot numbering, version conflicts, rebuilding reports).
- Increment requested by the user: $ARGUMENTS (treat as "snapshot" if empty).
- Inspect `git status --short` before editing; preserve unrelated user changes.
- Stop and ask the user before committing.
