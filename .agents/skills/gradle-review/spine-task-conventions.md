# Spine task conventions

This file is the authoritative source for Spine SDK rules on Gradle
custom tasks. The `gradle-review` skill enforces them, and
`practices/tasks.md` cross-references the rule alongside the upstream
Gradle "Best practices for tasks" page.

## Background: `group` and `description` are metadata

The `group` and `description` properties on a Gradle `Task` are
**metadata only**. They control how tasks are organised and displayed
in:

- `./gradlew tasks`
- The IntelliJ IDEA Gradle tool window
- Other build tools

They have **no impact** on task execution or task-dependency wiring.

Gradle and the Kotlin Gradle plugin intentionally place core tasks
(`compileJava`, `compileKotlin`, `processResources`, …) into the
**`other`** group to keep the default task list clean. High-level
tasks use the conventional groups `build`, `verification`,
`documentation`, and `publishing`.

## Rule

Every custom task registered or configured by Spine SDK code must set
both:

- **`group`** equal to the string `"spine"`. Use the shared constant
  once it exists — see
  [`../../tasks/spine-task-group-constant.md`](../../tasks/spine-task-group-constant.md).
- **`description`** as a short imperative sentence describing what
  the task does (no trailing period).

The rule applies to:

- `tasks.register(...) { … }` and `tasks.create(...) { … }`.
- `tasks.withType<…>().configureEach { … }`.
- Plugin production code that programmatically registers or
  configures tasks (`Plugin<Project>` implementations under
  `tool-base` and similar repos).

Both examples below reference the shared constant
`io.spine.gradle.SpineTaskGroup.name`, which holds the value
`"spine"` and is visible to every `build.gradle.kts` because it
lives in `buildSrc/`.

### Example — registering a new task

```kotlin
import io.spine.gradle.SpineTaskGroup

tasks.register("generateSpineModel") {
    group = SpineTaskGroup.name
    description = "Generates Spine model classes from .proto definitions"
    // ...
}
```

### Example — configuring an existing task type

```kotlin
import io.spine.gradle.SpineTaskGroup

tasks.withType<YourTaskType>().configureEach {
    group = SpineTaskGroup.name
    description = "Compiles Spine-specific module sources"
}
```

## Why this matters

- Makes Spine-specific tasks easy to discover in the IDE and on the
  command line, especially in large multi-plugin projects.
- Mirrors the convention established by Dokka, Ktlint, Shadow, and
  similar third-party plugins — each places its tasks in a single
  named group.
- Lets the `gradle-review` skill cross-check task registration code
  against one consistent rule.
