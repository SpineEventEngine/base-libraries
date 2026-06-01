---
slug: enforce-max-line-length
branch: address-gradle-review-01
owner: claude
status: draft
started: 2026-05-29
related-memories: []
---

## Goal

Extend the agent-facing instructions and skills under `.agents/` so
that detekt's `MaxLineLength` rule
(`buildSrc/quality/detekt-config.yml:19-21`,
`maxLineLength: 100`, `excludeCommentStatements: true`) is honoured at
author time and surfaced at review time, instead of being discovered
late by CI on GitHub.

Severity by file type:

- **Detekt-enforced → Must fix** — non-comment lines in `.kt` / `.kts`
  over the configured limit. These break `./gradlew build`.
- **Repo policy → Should fix** — KDoc / Javadoc body lines in any
  source extension; `.java` lines; `.proto` lines; `.md` lines
  (incl. `README.md`, `docs/**`, `.agents/**`). Detekt does not flag
  these; the reviewer skills do.

## Context

CI and local builds repeatedly fail on detekt's `MaxLineLength` rule.
The user finds the late discovery — especially on GitHub — annoying.
None of the current agent instructions or skills name the rule, so
agents write code that breaks the build, then have to retry.

### Framing

The numeric threshold is a configuration parameter, not a constant.

**Author-time behaviour**: agents read `MaxLineLength.maxLineLength`
from `buildSrc/quality/detekt-config.yml` once per session and treat
the value as a session-local constant. This is workable; re-reading
the YAML for every line of output is not.

**Guidance text**: the new sections never bake the literal number
into `.agents/` prose. They reference the rule name and the file
path. If the threshold changes, the agent's session-start lookup
picks up the new value with no doc edit.

**Review-time behaviour**: when a reviewer surfaces a finding, the
report cites the actual value (`"line 47 is 108 chars (limit 100,
from buildSrc/quality/detekt-config.yml)"`). The number lands in the
report, not in the rule.

### KDoc handling (empirically verified)

`excludeCommentStatements: true` excludes lines whose statement is a
comment — single-line `//`, trailing `//`, and KDoc body lines. The
exclusion of KDoc bodies is confirmed by
`buildSrc/src/main/kotlin/detekt-code-analysis.gradle.kts:52`, a
115-character KDoc body line that ships in the codebase and passes
the detekt build today. KDoc body lines are therefore Should-fix
repo policy, not Must-fix.

### Splitting / restructure rules (confirmed with user)

- String literals (including URLs inside strings) split at a
  meaningful boundary into ≥ 2 `+`-concatenated pieces — never
  truncated.
- Long imports: prefer an import alias
  (`import a.b.c.LongName as Short`). If unavailable, a
  `@file:Suppress("MaxLineLength")` is acceptable.
- Other unbreakable tokens (`[name][some.long.FQN]` in KDoc; long
  generated identifier): prefer restructure (intermediate `val`,
  reference-style Markdown link, alias). When no restructure is
  reasonable, use `@Suppress("MaxLineLength")` on the declaration
  with a brief `// Reason: …` comment. Use `@file:Suppress` only for
  file-scope cases (e.g., a long import that cannot be aliased).

### Scope clarifications

- **Generated sources excluded**: do not flag lines under
  `**/generated/**` or `**/generated-proto/**` — these are the paths
  Spine's `buildSrc/quality/checkstyle.xml:35-42` and
  `buildSrc/quality/pmd.xml:36-37` already exclude from the other
  static-analysis runs.
- **Reading context vs. reporting scope.** Reviewers continue to read
  each affected file fully (existing `kotlin-review` rule at
  `.agents/skills/kotlin-review/SKILL.md:31-32`). They only *report*
  line-length findings on lines the diff touched
  (`git diff -U0 <base>...HEAD`). Pre-existing long lines are not
  flagged. The two rules co-exist: read all, report changed.
- **`module.gradle.kts` carve-out**: per `AGENTS.md § Code review`,
  in a consumer repo `buildSrc/src/main/kotlin/module.gradle.kts` is
  in scope for the reviewers; it follows the same Must-fix rule as
  any other `.kts`.
- **YAML lookup is from `HEAD`, not the base ref.** Long-lived
  branches sometimes change `detekt-config.yml` mid-branch; reviewers
  always re-read the value from the working tree, so the rule matches
  what `./gradlew build` will see.
- **YAML missing is a hard error.** If
  `buildSrc/quality/detekt-config.yml` is absent or lacks
  `MaxLineLength.maxLineLength`, the reviewer reports a Must-fix
  asking the user to restore the config rather than silently
  inventing a number.

## Plan

Six `.agents/` Markdown files. No code or build changes. New lines
wrap at the configured limit.

### 1. `.agents/coding-guidelines.md`

- [ ] Add a new top-level `## Line length` section, placed immediately
      after the existing "Text formatting" section. The canonical
      content lives here; other docs cross-reference this heading.
      Cover:
  - Source-of-truth lookup: read `MaxLineLength.maxLineLength` from
    `buildSrc/quality/detekt-config.yml` once at session start. Never
    write the literal number into the guideline.
  - Severity split (detekt-enforced vs. repo policy) per Context above.
  - String-literal strategy with a small example whose split is at a
    URL path boundary, e.g.

    ```kotlin
    val ref = "https://github.com/SpineEventEngine/config/blob/master/" +
        "buildSrc/quality/detekt-config.yml"
    ```

    This covers the URL-splitting case the user called out; the
    existing `JacocoConfig.kt:122-125` pattern splits prose, not a
    URL, and is not a sufficient teacher on its own.
  - Unbreakable-token rules: import alias, restructure, then
    `@Suppress` placement (on the declaration; `@file:Suppress` for
    file-scope).
  - Scope exclusions: generated sources; changed lines only.

### 2. `.agents/documentation-guidelines.md`

- [ ] Append one bullet to "Commenting guidelines":

  > Wrap KDoc / Javadoc body lines and Markdown body lines at the
  > limit defined in `buildSrc/quality/detekt-config.yml`
  > (`MaxLineLength.maxLineLength`). See
  > `coding-guidelines.md § Line length` for the splitting strategy.

  Single sentence; no duplication of the canonical section.

### 3. `.agents/quick-reference-card.md`

- [ ] Rewrap the existing 135-char line 3 so the card itself respects
      the rule it now advertises.
- [ ] Append one line (plain text, no decorative emoji — the rest of
      the card uses 🚫 for a hard prohibition only, and line-length
      guidance isn't in that category):

  > At session start, read `MaxLineLength.maxLineLength` from
  > `buildSrc/quality/detekt-config.yml` and wrap new lines under it.
  > See `coding-guidelines.md § Line length`.

### 4. `.agents/skills/kotlin-review/SKILL.md`

- [ ] In "Review procedure" step 3 (the coding-guidelines checklist),
      append:

  > Line length (`MaxLineLength`). The reviewer reads the limit from
  > `buildSrc/quality/detekt-config.yml` and applies it only to lines
  > the diff touched. Non-comment `.kt` / `.kts` lines over the limit
  > are **Must fix** (detekt breaks the build;
  > `excludeCommentStatements: true` exempts KDoc bodies from the
  > build break). KDoc bodies in `.kt` / `.kts`, and any `.java` line
  > over the limit, are **Should fix**. For changed lines inside a
  > string literal the fix is splitting into ≥ 2 `+`-concatenated
  > pieces; otherwise follow `coding-guidelines.md § Line length`.

- [ ] Update "Output format" correspondingly: add the bucket entries
      but keep the existing Must / Should / Nits semantics unchanged.

### 5. `.agents/skills/review-docs/SKILL.md`

- [ ] Insert into "Checks → A. KDoc / Javadoc inside sources":

  > **Line length.** KDoc / Javadoc body lines wrap at the limit from
  > `buildSrc/quality/detekt-config.yml`. Long body lines are
  > **Should fix**; code lines around the comment, if also too long,
  > are owned by `kotlin-review`.

- [ ] Insert into "Checks → B. Markdown docs":

  > **Line length.** Body lines in `.md` — including `README.md`,
  > `docs/**`, and `.agents/**` (this expands the skill's prior `.md`
  > scope explicitly) — wrap at the configured limit. Long URLs go in
  > reference-style footnote definitions. Long lines are
  > **Should fix**.

### 6. `.agents/skills/pre-pr/SKILL.md`

- [ ] In the "Procedure" section, add a one-line pointer near the
      existing reviewer-dispatch table (around
      `.agents/skills/pre-pr/SKILL.md:104-106`):

  > Line-length findings on changed Kotlin / Java / Markdown lines
  > are reported by the dispatched reviewers (`kotlin-review`,
  > `review-docs`). pre-pr itself does not re-check.

  Documentation only — no logic change. Clarifies that the rule is
  inherited via the existing dispatch and prevents future edits from
  duplicating the check inside pre-pr.

### Verification

- [ ] Visually scan every edited file for the literal `100`. The
      number should not appear in the new prose; only the rule name
      and the YAML path should.
- [ ] Read the YAML, capture the value
      (`LIMIT=$(awk '/maxLineLength:/ {print $2}'
      buildSrc/quality/detekt-config.yml)`), and run
      `awk -v n=$LIMIT 'length > n' <each-edited-file>`. `awk`'s
      `length` counts bytes; for the ASCII prose introduced here that
      matches characters, but a non-ASCII glyph in future edits would
      miscount. Acceptable for this change.
- [ ] Sanity-check cross-references: every `coding-guidelines.md §
      Line length` link resolves to the new top-level section heading.
- [ ] Spot test the author behaviour. In a fresh session, ask the
      agent to write a long Kotlin string literal containing a URL;
      confirm the result splits with `+` at a URL path boundary and
      preserves every character.
- [ ] Spot test the reviewer behaviour. Synthesize a diff with: one
      non-comment `.kt` line over the limit (expect Must fix); one
      KDoc body line over the limit (expect Should fix); one `.java`
      line over the limit (expect Should fix); one `.md` body line
      over the limit (expect Should fix). Run `kotlin-review` and
      `review-docs` and confirm bucketing.
- [ ] Confirm the missing-YAML behaviour: temporarily move
      `buildSrc/quality/detekt-config.yml` aside, run a reviewer over
      a synthetic diff, confirm it reports a **Must fix** asking the
      user to restore the config (not a silent fallback).

## Out of scope

- `buildSrc/quality/detekt-config.yml` — unchanged.
- `writer/SKILL.md` and `java-to-kotlin/SKILL.md` — they author, they
  don't enforce. The canonical rule in `coding-guidelines.md` reaches
  them by reference.
- `gradle-review/SKILL.md` — `.kts` files are reviewed by
  `kotlin-review` (via pre-pr's `code` dispatch). Adding a second
  owner would double-report; defer to `kotlin-review § Line length`.
- `update-copyright/SKILL.md` — if a header rewrite produces a long
  line, the reviewer will catch it; no skill-local rule.
- `memory/MEMORY.md` and `_TOC.md` — the rule is durable team policy
  belonging in `.agents/`, indexed via the natural section heading.
- Rewrap of pre-existing over-length lines outside the diff (e.g.,
  `java-to-kotlin/SKILL.md:24,25,40,42`) — separate cleanup task, not
  blocked by this plan.

## Decisions

- **KDoc severity**. Should-fix, not Must-fix. Empirically verified
  by `buildSrc/src/main/kotlin/detekt-code-analysis.gradle.kts:52`
  (115-char KDoc body line that ships and builds clean).
- **`gradle-review` not edited**. `.kts` files flow through
  `kotlin-review` already (via pre-pr's `code` dispatch); a second
  owner in `gradle-review` would cause double-reports for the same
  finding. The trade-off is that manual `/gradle-review` runs without
  a paired `/kotlin-review` will not surface line-length findings on
  `.kts` files; users running only `gradle-review` are looking for
  Gradle conventions, not detekt rules, so the gap is acceptable.
- **YAML lookup at session start, not per line**. Re-reading the YAML
  for every line of output is impractical; the agent caches the value
  as a session-local constant. Documentation never bakes the literal.
- **Missing YAML is Must-fix, not informational**. Avoids silent
  fallback drift.

## Log

- 2026-05-29 — drafted in this session; plan revised twice to address
  findings from two review rounds (KDoc empirics, generated-source
  globs, `## Line length` heading placement, `gradle-review` →
  `pre-pr` swap, YAML-missing severity, verification cleanup).
  Awaiting approval.
