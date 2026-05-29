# JVM Project Requirements

General requirements for all JVM projects in the Spine SDK organisation.
Repo-specific `project.md` files link here and add their own context.

## Language and build

- **Languages**: Kotlin (primary), Java (secondary).
- **Build**: Gradle with Kotlin DSL.
- **Static analysis**: detekt, ErrorProne, Checkstyle, PMD.
- **Testing**: JUnit 5, Kotest Assertions, Codecov.

## Code review checklist

**Correctness and safety**
- Code compiles and passes static analysis (detekt, ErrorProne, Checkstyle, PMD).
- No reflection or unsafe code unless explicitly approved in scope.
- No analytics, telemetry, or tracking code.
- No blocking calls inside coroutines.

**Kotlin/Java style**
- Kotlin idioms preferred: extension functions, `when` expressions, data/sealed
  classes, immutable data structures.
- No `!!` unless provably safe. No unchecked casts.
- No mutable state without justification.
- No string duplication — use constants.

**Tests**
- New or changed functionality must include tests.
- Use stubs, not mocks.
- Prefer [Kotest assertions][kotest-assertions] over JUnit or Google Truth.

**Versioning**
- If the repo has `version.gradle.kts`, every PR must include a version bump.
  Flag the absence as a required change.

[kotest-assertions]: https://kotest.io/docs/assertions/assertions.html
