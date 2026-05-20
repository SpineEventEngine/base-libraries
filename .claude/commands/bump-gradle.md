---
description: Upgrade the Gradle wrapper to the latest release.
argument-hint: "[gradle-version]"
allowed-tools: Read, Edit, Bash(./gradlew:*), Bash(git status:*), Bash(git diff:*), WebFetch
---

Follow the `bump-gradle` skill exactly:

- Skill: `.agents/skills/bump-gradle/SKILL.md`
- Read the skill first.
- Use https://docs.gradle.org/current/release-notes.html as the source of truth for the latest version. Do NOT rely on remembered Gradle versions.
- If the user supplied a version: $ARGUMENTS, use it; otherwise read it from the release notes.
- Commit the wrapper change and dependency report change in separate commits per the skill.
