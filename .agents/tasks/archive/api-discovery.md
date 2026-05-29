---
slug: api-discovery
branch: improve-api-discovery
owner: claude
status: in-review
started: 2026-05-21
---

## Goal

Make Spine API discovery fast and token-efficient by directing agents
to read library sources from local sibling clones first, and from a
one-time-extracted sources-JAR cache otherwise — never via repeated
`unzip` against Gradle-cache JARs.

## Context

Investigation transcripts (see
`~/Desktop/gradle-caches-scanning-by-claude.png`) show agents running
dozens of `find ~/.gradle/caches` + `unzip -l` + `unzip -p` calls per
query. Each call decompresses the JAR; token usage is dominated by
path noise and JAR listings. The user keeps every Spine repo cloned
as a sibling under `/Users/sanders/Projects/Spine/`, so the raw
sources are already on disk for ~14 of the 16 Spine local deps and
just need to be reached directly.

Detailed design in `~/.claude/plans/mellow-juggling-yeti.md`.

## Plan

- [x] Draft plan + design review (Plan agent)
- [x] Write task file
- [x] Implement `lib/common.sh` (shared bash helpers)
- [x] Implement `discover` (main entry)
- [x] Implement `extract-sources` (one-shot JAR extraction, race-safe)
- [x] Implement `clean-cache` (manual pruning)
- [x] Write `README.md` + `.gitignore` for `.agents/scripts/api-discovery/`
- [x] Write `SKILL.md` for `.agents/skills/api-discovery/`
- [x] Add one bullet to `CLAUDE.md` under Workflow Rules
- [x] Smoke tests (#1–#7 from plan)
- [x] Code-review fixes (six findings, see Log)
- [x] **Follow-up:** `update-sibling` script + skill workflow for STALE
- [ ] Human review / merge; delete file on merge to master

## Follow-up: sibling auto-update on STALE

Originally deferred under "Out of scope" in the plan. User asked for
it: when STALE fires, the agent should offer to refresh the sibling
clone so api-discovery returns up-to-date sources. Constraints from
the user's reply:

- Pull only when the sibling is on its default branch (`master` or
  `main`) — they explicitly use checked-out feature branches as a
  staging area for "advancing multiple subprojects at the same time",
  so a feature branch is *intentional* local state and must be left
  alone.
- The action must be confirmed by the user, never autonomous.

Design:

- New script `update-sibling`:
  - Resolves a sibling by bare name (under `<workspace-root>`) or by
    absolute path.
  - Branch ∈ {`master`,`main`} + clean tree + tracked upstream
    → `git pull --ff-only`.
  - Any other branch → no-op, exit 0 with "using local code as-is".
  - Detached HEAD / dirty tree / no upstream → distinct exit codes
    (3 / 4 / 5) with descriptive stderr.
  - Pull failure → exit 6.
  - Never switches branches, never `--rebase`, never `--force`, never
    fetches a branch the user does not track.
- SKILL.md gains a "When STALE fires" section: surface the warning,
  ask the user, run `update-sibling` on consent, re-run `discover`
  if a pull happened.
- README.md documents the new script + adds it to the Layout table.

## Log

- 2026-05-21 — drafted plan; Plan-agent reviewed and pushed back on
  over-engineering (manifest, sibling repo, exit codes). Adopted
  simplifications.
- 2026-05-21 — cache location: `<workspace>/.agents/caches/api-discovery/`
  with first-use bootstrap prompt (approve / alt root / non-cached).
  Scripts and skill live in the consumer repo's `.agents/`.
- 2026-05-21 — implementation begins.
- 2026-05-21 — implementation complete. All 7 smoke tests pass.
  Resolved all 24 Spine artifacts end-to-end (Base/Change/Logging KMP/
  multi-module ProtoData/Validation/Tool-base/Mc-java). Extension-cache
  path tested with Jackson and Guava — concurrent extractions race
  safely on atomic `mv` with no `.tmp.*` leftovers. `STALE` warning
  fires for validation-java (declared .433 vs sibling .440). KMP
  source sets (`src/commonMain`, `src/jvmMain`, …) recognized in
  addition to plain `src/main`. Status flipped to `in-review` for
  human merge; task file will be deleted on merge to `master`.
- 2026-05-21 — added `update-sibling` follow-up: a guarded
  `git pull --ff-only` for stale Spine siblings. Branch ∈
  {`master`,`main`} + clean tracked tree + tracked upstream → pull;
  any other branch → no-op exit 0 ("intentional local state");
  detached / dirty / no-upstream → distinct refusals (exit 3/4/5);
  pull failure → exit 6. Uses `--untracked-files=no` so build
  artifacts and editor scratch don't block pulls. SKILL.md and
  scripts/README.md document the workflow; the agent must ask the
  user before invoking. Verified all 8 paths: successful FF on a
  synthetic master + upstream, no-op on `validation` (`address-issues`
  branch), exit 4 on `base-libraries` (dirty), exit 1 on missing,
  exit 2 on non-repo, exit 3 on detached HEAD, exit 5 on no upstream,
  exit 1 on missing args. The `main` default branch is also accepted.
- 2026-05-21 — `update-sibling` code-review pass applied five fixes:
  (a) README exit-0 row was missing the `already up-to-date` outcome.
  (b) `usage()` exited `EX_FAIL` (1), conflating "bad invocation" with
      "sibling not on disk". Added `EX_USAGE=64` (BSD `sysexits(3)`)
      and routed `usage()` to it; `sibling not on disk` keeps exit 1.
  (c) Reworded the dirty-tree guard comment: untracked files don't
      block FF on their own, but a genuine overwrite conflict (upstream
      adds a path that exists untracked locally) still surfaces via
      git's own check as `EX_PULL_FAILED`. Original "no effect on
      semantics" wording was misleading.
  (d) Exit 0 conflated three outcomes (pulled / up-to-date /
      skipped-branch) and the skill had to parse free-form English log
      lines to tell them apart. Now each success path emits a single
      stable stdout token (`pulled`, `up-to-date`, `skipped-branch`);
      failure paths emit empty stdout. Stderr keeps the human text.
  (e) `SKILL.md` rewritten around the token contract: the exit-code
      table splits exit 0 into three token-keyed sub-rows, procedure
      step 4 branches on the token (not stderr), and a new
      `up-to-date` example was added. `README.md` exit-code table got
      the same split plus an `EX_USAGE=64` row.
  Smoke-tested all eight paths end-to-end: synthetic upstream FF emits
  `pulled`, re-run on the same clone emits `up-to-date`, `validation`
  on `address-issues` emits `skipped-branch`, `base-libraries` (dirty)
  exits 4, missing path exits 1, non-repo exits 2, no-args/too-many
  exit 64. All failure paths produce empty stdout — agent can never
  misread an error message as a result token.
- 2026-05-21 — earlier code-review pass applied six fixes:
  (1) `extract-sources`: pre-test `[ -e "$target" ]` plus post-mv
      nested-debris cleanup. Previous version was unsafe because
      `mv tmp target` into an existing directory silently moves tmp
      INSIDE target on macOS/Linux instead of failing.
  (2) `discover`: replaced `target=$(...); status=$?` with
      `target=$(...) || exit $?` so `set -e` cannot terminate
      between assignment and status check.
  (3) `clean-cache`: added `prune_empty_parents()` and call it on
      both removal and "no entries match" paths (skipped under
      `--dry-run`). Empty `<group>/<artifact>/` dirs are now
      reclaimed.
  (4) `read_declared_version`: anchored regex at line start (with
      optional access modifier) to avoid matching `const val version`
      strings in KDoc / comments / nested code.
  (5) Removed dead `find_dep_file_for_artifact` (callers all use
      `find_local_dep_file_for_artifact`).
  (6) `find_local_dep_file_for_artifact`: validate artifact name
      against `[A-Za-z0-9._-]` and ERE-escape it via new
      `escape_ere()` before grep, blocking regex-metachar injection.
  All seven smoke tests still pass; race-safety verified by parallel
  extractions of `guava-testlib:33.5.0-jre` (both exit 0, no `.tmp.*`
  remnants, no nested `target/v.tmp.PID/` debris).
