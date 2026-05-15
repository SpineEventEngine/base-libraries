---
name: writer
description: >
  Write, edit, and restructure user-facing and developer-facing documentation.
  Use when asked to create/update docs such as `README.md`, `docs/**`, and
  other Markdown documentation, including keeping docs navigation data in sync;
  when drafting tutorials, guides, troubleshooting pages, or migration notes; and
  when improving inline API documentation (KDoc) and examples.
---

# Write documentation (repo-specific)

## Decide the target and audience

- Identify the target reader: end user, contributor, maintainer, or tooling/automation.
- Identify the task type: new doc, update, restructure, or documentation audit.
- Identify the acceptance criteria: “what is correct when the reader is done?”

## Choose where the content should live

- Prefer updating an existing doc over creating a new one.
- Place content in the most discoverable location:
  - `README.md`: project entry point and “what is this?”.
  - `docs/`: longer-form docs (follow existing conventions in that tree).
  - Source KDoc: API usage, examples, and semantics that belong with the code.

## Keep docs navigation in sync

- When adding, removing, moving, or renaming a page under
  `docs/content/docs/<section>/`, keep the current version's matching
  `sidenav.yml` in sync.
- Use `docs/data/versions.yml` to identify the current documentation version for
  that section. The current version is the entry with `is_main: true`; its
  `version_id` maps to `docs/data/docs/<section>/<version_id>/sidenav.yml`.
- Do not update historical version entries or their navigation files unless the
  user explicitly asks to edit that historical version.
- Map page files to `file_path` values relative to the current version's
  `content_path`, without `.md`; `_index.md` maps to its directory path, such as
  `01-getting-started/_index.md` -> `01-getting-started`.
- Keep each `page` label aligned with the page frontmatter `title` unless the
  existing navigation intentionally uses a shorter reader-facing label.
- Preserve the existing ordering, nesting, keys, comments, and YAML quoting
  style. Remove nav entries for deleted pages and update `file_path` values for
  moved pages.
- If a docs content change should not appear in navigation, say so explicitly in
  the final response.

## Follow local documentation conventions

- Follow `.agents/documentation-guidelines.md` and `.agents/documentation-tasks.md`.
- Use fenced code blocks for commands and examples; format file/dir names as code.
- When referencing a documentation page or section in body prose, use typographic
  double quotation marks only if the visible reference text is the actual page or
  section title, such as the “Getting started” page or the “Troubleshooting”
  section. The title normally starts with a capital letter. Do not add these
  quotes around generic or descriptive links such as “this page”, “the next
  section”, “declaring constraints”, or `4.3`, even if they point to a page or
  section. Do not add these quotes in “What’s next” sections or navigation
  elements. Keep file paths, identifiers, frontmatter values, navigation labels,
  and Markdown link labels in their expected syntax.
- In Markdown files, prefer footnote-style reference links for external `https://`
  targets instead of inline links. Write readable body text like
  `[label][short-id]`, then place the URL definition near the end of the file,
  such as `[short-id]: https://example.com/long/path`. Keep reference IDs short
  and descriptive. Inline links are still fine for local relative paths.
- Avoid widows, runts, orphans, and rivers by reflowing paragraphs when needed.

## Make docs actionable

- Prefer steps the reader can execute (commands + expected outcome).
- Prefer concrete examples over abstract descriptions.
- Include prerequisites (versions, OS, environment) when they are easy to miss.
- Use consistent terminology (match code identifiers and existing docs).

## KDoc-specific guidance

- For public/internal APIs, include at least one example snippet demonstrating common usage.
- When converting from Javadoc/inline comments to KDoc:
  - Remove HTML like `<p>` and preserve meaning.
  - Prefer short paragraphs and blank lines over HTML formatting.

## Validate changes

- For code changes, follow `.agents/running-builds.md`.
- For documentation-only changes in Kotlin/Java sources, prefer `./gradlew dokka`.
