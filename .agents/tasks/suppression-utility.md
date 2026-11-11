---
slug: suppression-utility
branch: claude/suppression-utility-baf0c6
owner: claude
status: in-review
started: 2026-11-11
---

## Goal

A utility in `base` that keeps the first of several failures and adds the later
ones to it as suppressed, usable from Java and Kotlin. core-jvm later replaces its
seven hand-written copies (and one in tests) with it, after bumping Base.

## Context

- core-jvm copies disagree: some catch only `RuntimeException` (an `Error` skips
  the remaining cleanup), and `Repository.attemptClose()` lacks the same-instance
  guard (`addSuppressed(self)` throws `IllegalArgumentException`).
- Home: `base/src/main/kotlin/io/spine/util/Cleanup.kt`
  (`@file:JvmName("Cleanup")`), next to `io.spine.util.Exceptions`. The utility uses
  only Java/Kotlin types, so it belongs in `io.spine.util`; `io.spine.base` hosts
  `ThrowableExtensions` because `causedByRejection` deals with Spine's
  `RejectionThrowable`. Kotlin lets one declaration serve as an extension for Kotlin
  and a static method for Java.

## Plan

- [x] Bump the version -> `2.0.0-SNAPSHOT.464` (working tree only; no commits in
      this session).
- [x] `T?.withSuppressed(next: T): T` in `Cleanup.kt` — generic, so `RuntimeException` in means
      `RuntimeException` out; never adds a failure to itself.
- [x] `runAll(vararg Runnable)` and `Iterable<AutoCloseable>.closeAll()` in
      `Cleanup.kt` — catch `Throwable`, throw the first failure at the end; unchecked as is, checked
      wrapped into `IllegalStateException` (later failures stay on the cause).
- [x] `runKeepingFailure(failure, step): Throwable?` + `throwIfAny(failure)` — run one
      step carrying a failure that may already exist (requested from core-jvm for
      `Repository.attemptClose()`, `BoundedContextBuilder.closeAfterFailure()`,
      `Application.Builder.buildContexts()`); restores the interrupt status before
      returning.
- [x] `ThrowableExtsTest.kt` -> `ThrowableExtsSpec.kt`: the old class name matched
      neither `*Test` nor `*Spec`, so the suite never ran.
- [x] `CleanupSpec` (Kotlin) + `CleanupJavaSpec` (Java bridge), in `io.spine.util`.
- [x] `./gradlew build dokkaGenerate` (includes `check`), read test counts.
- [x] `pre-pr`.

## Log

- 2026-11-11 — implemented; verifying.
- 2026-11-11 — `build dokkaGenerate` green after qualifying `java.lang.Error`
  (`io.spine.base.Error` is a proto message) and `AutoCloseable.close` links;
  `spine-code-review` APPROVE, `review-docs` prose fixes applied.
- 2026-11-11 — `kotlin-engineer` fixes: `closeAll` snapshots the iterable; the
  interrupt status is restored after the loop; fatal JVM errors stay caught, like
  `try`-with-resources (documented). Both new tests fail against the unfixed code.
  `build dokkaGenerate` green: 1165 tests, 0 failures (`base`: 1082). pre-pr PASS.
- 2026-11-11 — Owner chose a separate facade: `runAll`/`closeAll` moved to
  `Cleanup.kt` (`@file:JvmName("Cleanup")`, `io.spine.base`); specs split into
  `CleanupSpec` / `CleanupJavaSpec`. `build dokkaGenerate` green: 1165 tests,
  0 failures.
- 2026-11-11 — Owner: general-purpose helpers go to `io.spine.util`. Moved
  `Cleanup.kt` there, together with `withSuppressed`, so `io.spine.util` does not
  depend on `io.spine.base`. `ThrowableExts.kt` is back to its `master` content.
  `build dokkaGenerate` green: 1165 tests, 0 failures (`base`: 1082).
- 2026-11-11 — PR SpineEventEngine/base-libraries#970 opened.
- 2026-11-11 — core-jvm asked for a single-step helper; added `runKeepingFailure`
  and `throwIfAny`. Reviews: `spine-code-review` APPROVE, `kotlin-engineer` APPROVE,
  `review-docs` APPROVE WITH CHANGES; doc fixes applied (incl. the documented Java
  idiom `var unused = runKeepingFailure(t, …); throw t;`, compiled by the Java spec).
  `build dokkaGenerate` green: 1181 tests, 0 failures (`base`: 1098). Published
  `.464` to Maven Local for core-jvm; owner approved commit and push.
