---
name: bump-gradle
description: >
  Update the Gradle wrapper version used by this repository. Use when asked to
  upgrade Gradle, bump the Gradle wrapper, move the project to the latest
  Gradle release from the official release notes, run the Gradle build, and
  commit Gradle wrapper and dependency report changes separately.
---

# Bump Gradle

Use the official Gradle release notes as the source of truth for both the
latest version and the wrapper update command:

https://docs.gradle.org/current/release-notes.html#upgrade-instructions

Always check that page at task time. Do not rely on remembered Gradle versions.

## Checklist

1. Work from the target repository root.

   Confirm `./gradlew` and `gradle/wrapper/gradle-wrapper.properties` exist
   before changing anything. Inspect `git status --short` and preserve unrelated
   user changes. If Gradle wrapper files are already modified, inspect the diff
   and continue only when those edits are part of the same requested Gradle
   bump; otherwise ask before overwriting or staging them.

2. Read the latest Gradle version from the release notes.

   Open the Upgrade instructions section at the URL above. Use the version in
   the release heading and the wrapper command shown there. They should agree;
   if they do not, stop and report the mismatch.

3. Run the wrapper update command.

   Substitute the version from the release notes:

   ```bash
   ./gradlew wrapper --gradle-version=GRADLE_VERSION && ./gradlew wrapper
   ```

   For example, if the release notes say Gradle `9.5.1`, run:

   ```bash
   ./gradlew wrapper --gradle-version=9.5.1 && ./gradlew wrapper
   ```

4. Run the build.

   ```bash
   ./gradlew clean build
   ```

   If the wrapper update or build fails, do not commit partial changes. Report
   the failing command and the relevant error output.

5. Commit only Gradle-related files.

   Inspect `git status --short` and `git diff --name-only`. Stage only files
   created or updated by the Gradle wrapper bump, normally:

   ```text
   gradle/wrapper/gradle-wrapper.properties
   gradle/wrapper/gradle-wrapper.jar
   gradlew
   gradlew.bat
   ```

   Include other Gradle-owned files only when they are directly required by the
   wrapper update and are clearly part of the same change. Do not stage
   dependency reports or unrelated build output in this commit.

   Commit with the exact subject, replacing `GRADLE_VERSION`:

   ```text
   Bump Gradle -> `GRADLE_VERSION`
   ```

   Example:

   ```bash
   git commit -m 'Bump Gradle -> `9.5.1`'
   ```

   If no Gradle-related files changed, do not create an empty commit; report
   that the wrapper was already current after verification.

6. Commit dependency reports separately when the build updates them.

   Stage only generated dependency report files. In repositories using this
   config, the usual paths are:

   ```text
   docs/dependencies/pom.xml
   docs/dependencies/dependencies.md
   ```

   Include other changed files only when they are clearly generated dependency
   reports from the build. Commit them separately with:

   ```text
   Update dependency reports
   ```

7. Verify the final branch state.

   Confirm the recent commit subjects and make sure no owned Gradle bump or
   dependency report changes remain unstaged:

   ```bash
   git log --format=%s -2
   git status --short
   ```

   Leave unrelated pre-existing user changes alone and mention them separately
   in the final response.
