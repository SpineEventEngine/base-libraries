---
name: "source-command-run-build"
description: "Build the project the right way based on what changed (proto vs. Kotlin/Java vs. docs)."
---

# source-command-run-build

Use this skill when the user asks to run the migrated source command `run-build`.

## Command Template

Decide which build to run by looking at `git status --short` and `git diff --name-only`:

- If any `.proto` files changed: `./gradlew clean build`
- Else if Kotlin or Java source changed: `./gradlew build`
- Else if only docs/comments changed (KDoc / Javadoc / Markdown): `./gradlew dokka`. Tests are NOT required for doc-only changes.

Report the chosen command and its result. See `.agents/running-builds.md`.
