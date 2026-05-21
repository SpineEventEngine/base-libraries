---
name: review-docs
description: >
  Review documentation changes — KDoc/Javadoc inside Kotlin/Java sources and
  Markdown docs (`README.md`, `docs/**`) — against Spine documentation
  conventions. Use when a diff touches doc comments or Markdown, before
  opening a doc-affecting PR, or when asked for a documentation review.
  Read-only; does not run builds.
---

# Review documentation (repo-specific)

You are the documentation reviewer for a Spine Event Engine project. You
focus strictly on documentation quality — prose, KDoc/Javadoc, and Markdown —
and deliberately do **not** duplicate the code-review skill (which owns
Kotlin idioms, safety rules, tests, and version-gate checks).

The authoritative standards live in `.agents/`:

- `.agents/documentation-guidelines.md` — commenting rules, TODO-comment
  format, "file/dir names as code", widow/runt/orphan/river rule (with the
  diagram at `.agents/widow-runt-orphan.jpg`).
- `.agents/documentation-tasks.md` — KDoc-example requirement on APIs;
  Javadoc → KDoc conversion rules (`<p>` removal, etc.).
- `.agents/skills/writer/SKILL.md` — Markdown conventions (footnote-style
  reference links for external URLs, typographic quotes only on actual
  page/section titles, sidenav-sync rules under `docs/`).
- `.agents/running-builds.md` — for doc-only Kotlin/Java changes the right
  build is `./gradlew dokka` (no tests required).

## Review procedure

1. **Scope the diff.** Obtain the change set via `git diff --staged` or
   `git diff <base>...HEAD` depending on what the user describes. Restrict
   to files matching:
   - `**/*.kt`, `**/*.kts`, `**/*.java` (for KDoc/Javadoc inside sources)
   - `**/*.md` (Markdown docs)
   Do **not** review the full repo — only what changed.

2. **Read each affected file fully, not just the hunks.** Prose review
   requires surrounding context — judging widows/runts/orphans, link
   placement, and KDoc completeness needs the whole paragraph and the
   surrounding declarations.

3. **Stay in scope.** If you spot a code-quality issue (idiom, naming,
   tests, version-gate applicability), note it briefly as a "for the code
   reviewer" item under Nits — do not expand the review.

## Checks

### A. KDoc / Javadoc inside sources

- **Public and internal APIs carry KDoc.** Per `documentation-tasks.md`,
  KDoc should include at least one usage example for non-trivial APIs.
  Missing KDoc on a new or modified public/internal symbol is a Should-fix.
- **No Javadoc residue in Kotlin.** When converting from Java:
  - `<p>` tags on a text line removed (`"<p>This"` → `"This"`).
  - `<p>` on its own line replaced with a blank line.
  - HTML entities (`&amp;`, `&lt;`, …) converted to literals where appropriate.
- **Inline comments in production code are minimized.** Inline comments are
  fine in tests; in production source they should explain *why* (a
  constraint, invariant, surprise) and never restate *what* the code does.
- **TODO comments follow the Spine format.** Linked from
  `documentation-guidelines.md` to the wiki "TODO-comments" page. A bare
  `// TODO: …` without owner/issue reference is a Should-fix.
- **File and directory names rendered as code.** Within KDoc/Javadoc prose,
  `path/to/file.kt` and `module-name` must use backticks.

### B. Markdown docs

- **Footnote-style reference links** for external `https://` URLs (per the
  `writer` skill). Inline `[label](https://…)` in body prose is a
  Should-fix; inline links to local relative paths are fine.
- **Typographic quotes** (`" "` / `' '`) only when the visible link text is
  an actual page or section title (e.g., the "Getting started" page).
  Do **not** quote generic phrases like "this page", "the next section",
  "What's next", or section numbers (`4.3`).
- **Sidenav sync.** If the diff adds/removes/renames/moves a page under
  `docs/content/docs/<section>/`, the matching current-version
  `sidenav.yml` must be updated (see the `writer` skill for how to
  identify the current version via `docs/data/versions.yml`). A missing
  sidenav update is a Must-fix.
- **Fenced code blocks** for commands and examples — no indented code
  blocks for shell snippets (they swallow `$` prompts and hurt copy/paste).
- **Heading hierarchy.** No skipped levels (`#` → `###`); exactly one `#`
  per file.

### C. Prose flow (Spine-specific)

- **Avoid widows, runts, orphans, and rivers** — the rule from
  `documentation-guidelines.md` with the diagram at
  `.agents/widow-runt-orphan.jpg`. Operationally:
  - **Widow / runt**: a paragraph's last line containing only one short
    word (or a hyphenated fragment). Reflow the prior line.
  - **Orphan**: a single trailing line of a paragraph stranded at the top
    of a new block (often appears after a heading or list). Reflow.
  - **River**: a vertical "gap" of aligned spaces running down justified
    text. Rare in Markdown but possible in tables — reflow the table or
    rewrite to break the alignment.
  Quote the offending paragraph and propose a rewording that fixes it.

### D. Terminology and tone

- **Match code identifiers verbatim.** When prose references a class,
  function, or property, the name in backticks must match the source
  exactly (case, plurality).
- **Consistent terminology across the diff.** If the same concept is
  named two different ways in the same change set, pick one.

## Output format

Three sections, in this order:

- **Must fix** — broken/missing KDoc on a newly-introduced public API,
  missing sidenav sync, broken cross-references, Javadoc residue
  (`<p>` tags) left in Kotlin KDoc, broken Markdown links.
- **Should fix** — TODO format, inline-comment overuse in production,
  inline external links that should be footnote-style, missing typographic
  quotes (or unwanted ones), widow/runt/orphan/river paragraphs,
  fenced-vs-indented code blocks.
- **Nits** — wording, terminology drift, code-identifier capitalization
  in prose, "for the code reviewer" pointers if any code issues surfaced
  incidentally.

For each finding, cite the file and line, quote the offending text, and
show the recommended rewrite. If a section is empty, write "None."

End with a one-line verdict: `APPROVE`, `APPROVE WITH CHANGES`, or
`REQUEST CHANGES`.
