---
slug: setup-cross-tool-agent-instructions
branch: improve-caching
owner: claude
status: in-review
started: 2026-05-24
---

# Task: Consolidate Agent Instructions into AGENTS.md

## Goal

Move universal agent instructions from `CLAUDE.md` into `AGENTS.md` so that
Claude Code, GitHub Copilot, and Codex all read identical rules from a single
source. Reduce `CLAUDE.md` to a thin wrapper that imports `AGENTS.md` plus a
small Claude Code-specific section.

## Current state

Both files already exist with real content.

**`AGENTS.md`** currently has:
- Orientation — `project.md` reference, link to `.agents/_TOC.md`
- Commit and history safety — full rule (authoritative)
- Other safety rules — compile check, no auto-deps, no analytics, no reflection
- Moving files — `git mv` rule

**`CLAUDE.md`** currently has:
- Project Guidelines — quick-reference-card, `project.md`, `jvm-project.md`,
  skills, TOC
- Workflow Rules — `EnterPlanMode`, task planning, `api-discovery` skill,
  commit rule (duplicate of AGENTS.md)
- Memory — team memory (`.agents/memory/`) + per-developer (auto-memory)
- Verification & Quality
- Core Principles
- Task Flow — plan writing, `ExitPlanMode`, `TaskCreate`
- Final Rule

## Content split

**Universal — move to `AGENTS.md`:**

| Section | Notes |
|---|---|
| Project Guidelines (project.md, jvm-project.md, skills, TOC) | All agents need this orientation |
| Memory → team-shared store only (`.agents/memory/`) | Codex/Copilot have no auto-memory; the team store is universal |
| Verification & Quality | Universal engineering standards |
| Core Principles | Universal |
| Task Flow items 1, 4, 5, 6 (plan write, verify, update memory, delete task) | Universal; omit items 2–3 (ExitPlanMode/TaskCreate) |

**Claude Code-specific — keep in `CLAUDE.md` only:**

| Item | Why Claude-only |
|---|---|
| `EnterPlanMode` / `ExitPlanMode` | Claude Code SDK tools |
| `api-discovery` skill / never unzip JARs | Gradle cache path is machine-local |
| Per-developer auto-memory | Claude Code built-in feature |
| `TaskCreate` for live status | Claude Code SDK tool |
| Final Rule meta-note | Claude Code session advice |

## Steps

### 1. Expand `AGENTS.md`

Add the universal sections to `AGENTS.md` after the existing content. Do not
duplicate the commit rule — it is already there. Resulting sections in order:

1. Welcome / Orientation *(already exists — update to include quick-reference-card and skills references)*
2. Commit and history safety *(already exists — keep as-is)*
3. Other safety rules *(already exists — keep as-is)*
4. Moving files *(already exists — keep as-is)*
5. **Memory** — team-shared store only; omit the per-developer store
6. **Verification & Quality**
7. **Core Principles**
8. **Task planning** — write plan to `.agents/tasks/<slug>.md`; verify before marking done; delete task file on merge

Keep `AGENTS.md` under 120 lines. Every line must change agent behaviour.

### 2. Rewrite `CLAUDE.md` as a thin wrapper

Replace the current content with:

```markdown
@AGENTS.md

## Claude Code-specific notes

- Use Plan mode (`EnterPlanMode`) for architecture, refactoring, multi-file
  changes, or lengthy documentation. Show the plan (`ExitPlanMode`) before
  implementing.
- Track live progress with `TaskCreate`.
- Before reading library source code from `~/.gradle/caches`, follow the
  `api-discovery` skill — never `unzip` JARs directly.
- Per-developer memory lives in the built-in auto-memory dir. Use it for
  personal preferences, ephemeral project state, and per-machine resources.
  Litmus test: *would a teammate benefit from this next month?* → repo.
  Otherwise → auto-memory.
- This is living team memory. Update it regularly and keep it concise
  (<120 lines / ~2.5k tokens).
```

### 3. Verify `.github/copilot-instructions.md`

This file already exists. Confirm it contains an explicit reference to `AGENTS.md`
at the repository root, a pointer to `project.md` for repo context, and the
universal "Do not suggest" safety rules. Add the `AGENTS.md` reference if absent.

### 4. Verify the setup

Run these checks and report results:

- `AGENTS.md` exists at repo root and is under 120 lines (`wc -l AGENTS.md`).
- `CLAUDE.md` first non-empty line is `@AGENTS.md`.
- `.github/copilot-instructions.md` exists and references `.agents/project.md`.
- All modified files are tracked by git (no relevant "Untracked files" in
  `git status`).

### 5. Commit

Stage only the files modified by this task. Use this commit message:

```
refactor: consolidate agent instructions into AGENTS.md

Move universal rules (orientation, memory, quality, principles, task
planning) from CLAUDE.md into AGENTS.md so Codex, Copilot, and Claude
Code all read from a single source. CLAUDE.md becomes a thin @AGENTS.md
wrapper plus Claude Code-specific notes.
```

## Acceptance Criteria

- Editing `AGENTS.md` is the only required change to update agent behaviour
  across all three tools.
- No universal instruction content exists only in `CLAUDE.md`.
- `AGENTS.md` is under 120 lines.
- `CLAUDE.md` first non-empty line is `@AGENTS.md`.
- All checks in step 4 pass.
