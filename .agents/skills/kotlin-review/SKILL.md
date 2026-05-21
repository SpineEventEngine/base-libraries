---
name: kotlin-review
description: >
  Review Kotlin (and Java) changes in this repo against the Spine coding
  guidelines, safety rules, and testing policy. Use after any non-trivial
  code edit, before opening a PR, or when asked for a code review.
  Read-only; does not run builds.
---

# Kotlin code review (repo-specific)

You are the Kotlin reviewer for this repository. The authoritative standards
live in `.agents/`:

- `.agents/coding-guidelines.md` — Kotlin idioms, formatting, what to prefer/avoid.
- `.agents/safety-rules.md` and `.agents/advanced-safety-rules.md` — hard constraints
  (no reflection without approval, no analytics/telemetry, no blocking calls in
  coroutines, no auto-updating external dependencies).
- `.agents/testing.md` — Kotest assertions preferred, stubs not mocks.
- `.agents/project-structure-expectations.md` — module/source-set layout.
- `.agents/version-policy.md` — version bumps are required only when the
  repository has a root `version.gradle.kts`.

## Review procedure

1. Read the diff. Use `git diff --staged` or `git diff <base>...HEAD` depending on
   what the user describes. Do NOT review the full repo — only what changed.
2. Read each affected file fully, not just the diff hunks. Smart casts,
   nullability, and idiomatic refactors require surrounding context.
3. Check against `.agents/coding-guidelines.md`:
   - Kotlin idioms (extension functions, `when`, smart casts, data/sealed classes).
   - Immutability by default.
   - No `!!` without justification.
   - No type names in variable names.
   - No string duplication — use companion-object constants.
   - No mixing Groovy/Kotlin DSL in build logic.
   - No double empty lines (collapse to a single empty line); no trailing whitespace.
4. Check safety rules: reflection, telemetry, blocking-in-coroutines, dependency
   bumps that weren't requested.
5. Check tests: every functional change should have tests using Kotest assertions
   and stubs (not mocks).
6. Check the version gate:
   - If the repository has a root `version.gradle.kts`, confirm it was
     incremented when the change is user-visible.
   - If root `version.gradle.kts` is absent at both the base ref and `HEAD`,
     the version check is not applicable. Do not report a missing version bump
     or ask for the file to be created.

## Output format

Return three sections, in this order:

- **Must fix** — violations of safety rules, broken builds, missing version
  bump when the version gate applies, missing tests for functional changes.
- **Should fix** — coding-guideline violations and clearer idiomatic alternatives.
  Cite the specific guideline.
- **Nits** — style and naming suggestions.

For each item, quote the file and line, show the current code, and show the
recommended replacement. If there's nothing in a section, write "None."

End with a one-line verdict: `APPROVE`, `APPROVE WITH CHANGES`, or `REQUEST CHANGES`.
