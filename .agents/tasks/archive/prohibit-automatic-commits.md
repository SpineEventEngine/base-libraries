---
slug: prohibit-automatic-commits
branch: prohibit-automatic-commits
owner: claude
status: in-review
started: 2026-05-20
---

## Goal

Make it a durable, team-wide rule that AI agents (Claude Code main thread,
every subagent, every skill) MUST NOT run `git commit` (or other
history-writing git/gh operations) unless authorization is *explicit and
current*. Authorization comes from one of two sources only:

1. The currently active skill's `SKILL.md` contains an explicit
   `## Commit authorization` section.
2. The user's current prompt explicitly instructs the operation
   (e.g. "commit this", "push the branch").

Agents must otherwise stage changes and stop, letting the user review and
decide. This preserves today's auto-commit behavior for `bump-version`
and `bump-gradle`, which will declare authorization in their SKILL.md.

## Context

- Today's pain: Claude Code commits routinely, even when the user wants
  to review diffs locally first.
- The project's `.claude/settings.json` already has `Bash(git commit:*)`
  in `permissions.ask`. That asks the user per-commit but does not
  redirect agent behavior — the agent still proposes commits constantly.
  The fix is at the *instruction* layer, not the permission layer.
- Skills that legitimately commit today: `bump-version`, `bump-gradle`.
- Skills that do not commit but prescribe commit messages for the human:
  `dependency-update` (already says "Do not commit. Do not push.").
- The user accepted removal of the global `~/.claude/settings.json` hook
  added earlier this session. Enforcement lives in `.agents/` instructions
  only.

## Plan

- [x] **1. Add the canonical rule to `.agents/safety-rules.md`.**
  Added section *Commits and history-writing*. Lists default (no
  history writes), two authorization sources, the fallback behavior
  (stage + show diff + stop), and the operations covered. Names the
  `## Commit authorization` marker.

- [x] **2. Surface the rule in `.agents/quick-reference-card.md`.**
  Added one-line pointer to `safety-rules.md` → *Commits and
  history-writing*.

- [x] **3. Add a workflow rule to `CLAUDE.md`.**
  Added bullet under *Workflow Rules* referencing
  `.agents/safety-rules.md`.

- [x] **4. Declare authorization in `bump-version/SKILL.md`.**
  Added a top-level `## Commit authorization` section above the
  Checklist: exactly one commit, stage only `version.gradle.kts`,
  subject `` Bump version -> `<new>` ``, no push/tag/amend.

- [x] **5. Declare authorization in `bump-gradle/SKILL.md`.**
  Added a top-level `## Commit authorization` section above the
  Checklist: up to two commits (wrapper + dependency reports), exact
  subjects, no push/tag/amend.

- [x] **6. Cross-check the non-authorizing skills.**
  `dependency-update/SKILL.md` already explicit ("Do not commit. Do
  not push.") — left as is. `pre-pr/SKILL.md` does not commit — left
  as is. Other skills scanned (see Log).

- [x] **7. Verification.** See Log entry — all three grep checks pass.

## Out of scope

- Project `.claude/settings.json` `ask` rule for `Bash(git commit:*)`:
  leave as defense-in-depth (zero cost when the agent obeys the rule).
- `~/.claude/settings.json` global hook: already reverted earlier this
  session per user direction.

## Log

- 2026-05-20 — drafted, awaiting plan approval.
- 2026-05-20 — approved by user. Executed steps 1–6.
- 2026-05-20 — verification:
  - `grep -RIn '^## Commit authorization' .agents/skills/` returns exactly
    `bump-gradle/SKILL.md` and `bump-version/SKILL.md` ✓
  - `safety-rules.md` referenced from `CLAUDE.md`, `quick-reference-card.md`,
    `bump-version/SKILL.md`, `bump-gradle/SKILL.md` ✓
  - Literal `git commit` strings live only in the two authorizing skills ✓
  - `dependency-update/SKILL.md` still says "Do not commit. Do not push.";
    `pre-pr/SKILL.md` still writes a sentinel and does not commit ✓
- Status: `in-review` — awaiting user sign-off, then delete on merge to master.
