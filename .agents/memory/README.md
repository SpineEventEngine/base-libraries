# Team memory — `.agents/memory/`

Validated patterns, durable project context, and pointers to external
systems. Checked into git so the whole team — and any agent working in
this repo — benefits from accumulated knowledge.

This complements Claude Code's built-in per-developer auto-memory:
team-shareable knowledge lives here; personal preferences and ephemeral
state live in the auto-memory.

## Layout

    .agents/memory/
    ├── MEMORY.md           # Index — scan at start of every session
    ├── README.md           # This file — read when adding/updating memories
    ├── feedback/           # Validated patterns & corrections
    ├── project/            # Durable project context & rationale
    └── reference/          # External systems & resources

One file per memory. Filename = the memory's kebab-case slug.

## File format

    ---
    name: tests-no-db-mocks
    description: One-line summary — used to surface relevance, so be specific.
    metadata:
      type: feedback           # feedback | project | reference
      since: 2026-05-19        # date added (ISO)
    ---

    <one-paragraph rule or fact>

    **Why:** <reason — incident, constraint, team convention>

    **How to apply:** <when this kicks in; what to do or avoid>

    Related: [[other-memory-slug]]

`Why:` and `How to apply:` are required for `feedback` and `project`
memories — they let future readers judge edge cases. `reference`
memories may be shorter (link + one-line purpose).

Link related memories with `[[slug]]` (the target file's `name:`).

## Routing — repo vs. auto-memory

| Kind of fact | Goes to |
|---|---|
| Personal preference, role, style | auto-memory (`user`) |
| Personal habit feedback | auto-memory (`feedback`) |
| Team coding/test/PR rule | **`feedback/`** |
| Durable project rationale | **`project/`** |
| Ephemeral project state (freezes, OOO, deadlines) | auto-memory (`project`) — would rot in git |
| Team-shared external resource | **`reference/`** |
| Personal external resource | auto-memory (`reference`) |

**Litmus test:** *would a teammate joining the project next month benefit
from knowing this?* If no, it belongs in auto-memory.

## Write protocol

1. Write the file **uncommitted** in the working tree.
2. **Surface the change** in the same turn so the human can review.
3. **Do not auto-commit** memory edits as part of an unrelated PR — memory
   changes should be reviewable on their own.
4. **Correct in place** when an existing memory turns out wrong; `git blame`
   carries the history.
5. **Propose deletion explicitly** when a memory has gone stale, rather
   than silently editing it out.

## Updating the index

After adding or removing a memory file, update `MEMORY.md`. One line under
the matching section:

    - [slug](category/slug.md) — description from frontmatter

Keep the index short — long descriptions belong in the file body.

## Anti-patterns — do not store

- Anything derivable from the code (module structure, paths, conventions
  visible in source). Use `grep` / `Read`.
- Recent-activity summaries or PR lists — `git log` is authoritative.
- Fix recipes for specific bugs — the commit message belongs in the commit.
- Anything already documented in `.agents/` reference docs — keep one
  source of truth.
- Personal preferences (see routing).
