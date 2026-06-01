# Gradle best-practices index

This directory mirrors selected pages of the upstream Gradle "Best
practices" user guide. Each file is derived from one Gradle docs page
and links back to its source URL. The `gradle-review` skill references
these files when reviewing changes.

## Gradle version pin

The notes here track Gradle **9.5.1** — the version pinned by
`gradle/wrapper/gradle-wrapper.properties` in this repository at the
time of ingest. When the wrapper is bumped, refresh each `*.md` below
against the matching `docs.gradle.org/<version>/userguide/...` page and
update this section.

## Ingested pages

| File | Source | Last reviewed |
|------|--------|---------------|
| [tasks.md](tasks.md) | <https://docs.gradle.org/9.5.1/userguide/best_practices_tasks.html> | 2026-05-29 |

## Ingest procedure

Ingests are **user-initiated only.** This procedure runs when a
maintainer explicitly asks for a new practice page or for a refresh
(typically after a Gradle wrapper bump). The skill never auto-fetches
Gradle docs.

1. Identify the Gradle docs page URL.
2. Pick a slug from the page's anchor (e.g. `tasks`, `dependencies`,
   `configurations`). Keep slugs short and kebab-case.
3. Create `practices/<slug>.md` with this frontmatter:

       ---
       source: <full URL>
       gradle-version: <X.Y[.Z] from the wrapper>
       ingested: <YYYY-MM-DD>
       ---

4. For each best practice on the page, write a short section with:
   - **The rule.** One sentence.
   - **Why it matters.** One sentence — the rationale Gradle cites.
   - **Spine review level.** One of `Must fix`, `Should fix`, `Nit`.
     Map upstream "recommended" items by the failure mode they
     prevent: build-correctness failures or lost task dependencies →
     `Must fix`; cache-miss performance and idiomatic concerns →
     `Should fix`; style and naming → `Nit`.

5. If the page introduces a category not covered by the current
   `SKILL.md` "Check upstream Gradle best practices" list, edit that
   list.

6. Add a row to the table above. Bump the `Last reviewed` date.

## Spine additions

Some `gradle-review` checks have no direct upstream counterpart but
follow from existing Spine guidelines:

- **`tasks.create(...)` vs. `tasks.register(...)`** — Spine prefers
  lazy registration. The rule cross-references the `@since 4.9`
  Gradle documentation on lazy configuration but is enforced as a
  Spine review item.
- **Mixing Groovy and Kotlin DSL** — Spine projects use Kotlin DSL
  exclusively (`*.gradle.kts`, `*.kt`).

These are documented inside the relevant `practices/*.md` "Spine
additions" sections so reviewers see them alongside the upstream rules.
