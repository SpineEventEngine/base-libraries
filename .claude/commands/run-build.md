---
description: Build the project the right way based on what changed (proto vs. Kotlin/Java vs. docs).
allowed-tools: Bash(./gradlew:*), Bash(git status:*), Bash(git diff:*)
---

Decide which build to run by looking at `git status --short` and `git diff --name-only`:

- If any `.proto` files changed: `./gradlew clean build`
- Else if Kotlin or Java source changed: `./gradlew build`
- Else if only docs/comments changed (KDoc / Javadoc / Markdown): `./gradlew dokka`. Tests are NOT required for doc-only changes.

Report the chosen command and its result. See `.agents/running-builds.md`.
