# Task plans — `.agents/tasks/`

Durable task plans. Checked into git so the whole team — and any
agent working in this repo — can review, resume, or pick up
sub-tasks across sessions.

This complements Claude Code's built-in Plan mode and in-session
task list: the file here is the durable source of truth; the
built-in tools gate approval and render live progress.

## Layout

    .agents/tasks/
    ├── README.md           # This file
    └── <slug>.md           # One file per task; status in frontmatter

Filename = the task's kebab-case slug. Multiple active tasks per
branch are allowed — use distinct slugs.

## File format

    ---
    slug: add-team-memory
    branch: tune-claude
    owner: claude              # or a human/agent handle
    status: in-progress        # see status values below
    started: 2026-05-19
    related-memories:          # optional — links into .agents/memory/
      - team-memory-routing
    ---

    ## Goal
    <one-paragraph statement of what success looks like>

    ## Context
    <links, constraints, why now>

    ## Plan
    - [x] Step 1
    - [ ] Step 2
      - notes / sub-bullets
    - [ ] Step 3

    ## Log
    - 2026-05-19 14:02 — drafted, awaiting approval
    - 2026-05-19 14:15 — approved, executing
    - 2026-05-19 14:42 — step 3 blocked on …

The checklist uses `- [ ]` / `- [x]` so another agent can claim and
complete unchecked items by ticking them and adding a `Log` line.

### `status` values

| value | meaning |
|---|---|
| `draft` | written but not yet approved |
| `approved` | approved, not yet started |
| `in-progress` | execution under way |
| `blocked` | paused; reason in `Log` |
| `in-review` | work done, awaiting review |
| `done` | complete — file is then deleted (see lifecycle) |

## Workflow

1. **Discover** — at task start, scan `.agents/tasks/` for
   in-progress or blocked plans on the current branch. Resume
   rather than restart.
2. **Draft** — write `<slug>.md` with `status: draft` and the
   plan checklist.
3. **Approval gate** — `EnterPlanMode` → `ExitPlanMode`. The plan
   presented to the human references the file path; the human may
   edit the file directly before approving.
4. **Mirror** — on approval, flip `status: approved` → `in-progress`
   and populate `TaskCreate` from the top-level checklist for live
   in-session progress.
5. **Execute + sync** — use `TaskUpdate` for fine-grained progress.
   Edit the file only at meaningful checkpoints: step done, blocker,
   scope change, new note.
6. **Complete** — flip `status: done`. The file is raw material for
   the PR description.
7. **Delete on merge** — once the branch lands on master, delete the
   task file in the same commit or shortly after. `git log --follow`
   recovers it if ever needed.

## Cross-agent coordination

- Other agents (or other CC sessions) `Read` the file to pick up
  state. They MUST update `status`, tick checkboxes, and append
  `Log` lines rather than rewriting the plan silently.
- If two agents work the same task in parallel, partition by
  checkbox — each agent claims unchecked items by tagging the line
  (e.g. `- [ ] (owner: reviewer-bot) Run dependency-audit`) or by
  appending a `Log` line.
- The **file** is the contract. In-session `TaskCreate` state is
  per-session and not authoritative.

## When to create a task file

Create one whenever the work is non-trivial:

- Changes spanning multiple files or modules (features, refactors).
- Lengthy documentation work — multi-page guides, restructuring
  `docs/`, migration notes, tutorials. The task file plans and
  tracks the effort; the docs-related skills (`writer`,
  `write-docs`, `review-docs`) handle individual page work inside
  the plan steps.
- Cross-agent or cross-session work (e.g. one agent drafts, another
  reviews).
- Anything that may span sessions and needs durable state.

Do **not** create a task file for:

- Trivial changes (single-file edits, typo fixes, version bumps) —
  pure overhead.
- Deliverables themselves — code lives in source, docs in `docs/`,
  design records where the project keeps them. Task files describe
  the *work*, not the artifact.
- Status reports of work already done — that's a `Log` entry on an
  existing task, or the PR description.
- Personal reminders / todo lists — use the built-in task list.

## Relationship to other stores

- **`.agents/memory/`** — enduring lessons that survive *across*
  tasks. If a task yields a generalizable rule, add the memory and
  link from the task's `related-memories`.
- **Built-in auto-memory** — personal and ephemeral. Task files do
  not carry personal preferences.
