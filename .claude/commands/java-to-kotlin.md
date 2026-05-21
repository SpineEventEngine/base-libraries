---
description: Convert Java files to idiomatic Kotlin, including Javadoc -> KDoc.
argument-hint: "<path-to-java-file-or-dir>"
allowed-tools: Read, Edit, Write, Bash(./gradlew:*), Bash(git status:*), Grep, Glob
---

Follow the `java-to-kotlin` skill exactly:

- Skill: `.agents/skills/java-to-kotlin/SKILL.md`
- Target: $ARGUMENTS
- Preserve behavior. Convert Javadoc to KDoc, `@Nullable` to nullable Kotlin types, getters/setters to properties, static methods to companion objects or top-level functions.
- After each file, run `./gradlew compileKotlin` (or the relevant module's compile task) to verify.
- Honor `.agents/coding-guidelines.md` for Kotlin idioms.
