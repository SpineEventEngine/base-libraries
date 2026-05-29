---
slug: prompt-caching-org
branch: improve-caching
owner: claude
status: in-review
started: 2026-05-24
related-memories: [cache-warm-window, anthropic-api-caching]
---

## Goal

Maximise Claude API prompt cache hit rates across the Spine GitHub organisation
(~40 sibling repos) so that repeated session starts and agent invocations read
from cache at 0.1× token cost rather than processing the full prompt fresh.

## Context

- Claude Code already applies automatic prompt caching to every API call it
  makes. There is no single "enable" switch; the work is about raising the
  cache hit rate and keeping it high.
- The `migrate` script overwrites `CLAUDE.md`, `.agents/`, `.claude/`, and
  `buildSrc/` in each sibling repo with an exact copy from this repo. This
  means all 40 repos hold byte-identical content after a `./config/pull` and
  therefore share the same cache entry at any given config version.
- The `openai.yaml` files under each skill are FleetView UI interface metadata
  only — they define display name and default prompt, not API call parameters.
  `cache_control` cannot go there.
- No GitHub Actions workflow currently calls the Anthropic API directly.
- Current stable prefix: CLAUDE.md (≈ 900 tokens) + quick-reference card
  (≈ 200 tokens) ≈ 1,100 tokens.
  - This **clears** the 1,024-token minimum for Sonnet 4.6 / Opus.
  - This **does not meet** the 4,096-token minimum for Haiku 4.5.
- The team memory system is empty; populating it will grow the stable prefix.
- Cache TTL defaults to 5 minutes. Sessions more than 5 minutes apart miss
  the cross-session cache unless the extended 1-hour TTL is used.

## Plan

- [ ] **Step 0 — Diagnose why zero caching is happening and enable it**

  The Console Caching dashboard ("TeamDev Management OÜ", All workspaces) shows
  no prompt caching in use — no `cache_control` blocks are being sent by any
  caller. This is the highest-priority item; the remaining steps only add value
  once caching is active.

  Sub-tasks:

  - **0a. Switch to Console OAuth on every developer machine**

    Raw API key auth loses per-developer identity (`email`, `orgId`, `orgName`
    all null in `claude auth status`). Console OAuth preserves identity while
    still billing to "TeamDev Management OÜ".

    **For each developer:**
    1. Remove `ANTHROPIC_API_KEY` from `~/.claude/settings.json` — it takes
       precedence over OAuth in the auth stack and must be absent.
    2. Run `claude` → a browser window opens → log in with Console credentials
       (the same account used at console.anthropic.com).
    3. Run `claude auth status` and confirm `email`, `orgId`, `orgName` are
       populated.

    **For the org admin (Alexander):**
    - Invite the second developer via Console → Settings → Members → Invite.
    - Assign the "Developer" or "Claude Code" role.
    - They accept the email invite, then follow the three steps above.

  - **0b. Enable 1-hour cache TTL on every developer machine**

    Console OAuth users get the **5-minute** default cache TTL — the 1-hour
    TTL is only automatic for claude.ai subscription users. Add the opt-in
    to `~/.claude/settings.json` on every developer machine:

    ```json
    {
      "env": {
        "ENABLE_PROMPT_CACHING_1H": "1"
      }
    }
    ```

    Restart Claude Code after saving. This is the highest-impact change in
    the entire plan — without it, cache entries expire every 5 minutes and
    cross-session hits are rare.

  - **0c. Verify caching is active** — start a Claude Code session, make a
    few turns, wait 2–3 minutes, then check Analytics → Usage in the Console
    under "TeamDev Management OÜ". Non-zero `cache_creation_input_tokens`
    confirms caching is active. Non-zero `cache_read_input_tokens` on a
    subsequent session in the same hour confirms hits are occurring.

  - **0d. Investigate remote skill calls** — FleetView-managed remote skills
    (the 7 skills with `openai.yaml`) make their own API calls through the
    agent platform. Confirm whether those calls include `cache_control`; if
    not, this may require configuration in the FleetView platform outside
    this repo.

  Until steps 0a–0b are done on both developer machines, Steps 1–3 improve
  future cache hygiene but produce limited cost savings.

- ~~**Step 1 — Cache-hygiene team memory**~~ — *reverted 2026-05-25: the
  batching guidance was too restrictive on `config` changes; removed
  `.agents/memory/feedback/cache-hygiene.md` and its references.*

- [x] **Step 2 — Post-migration cache-warm window (reference memory)**

  Create `.agents/memory/reference/cache-warm-window.md` documenting:
  - Realistic concurrency is 1–2 developers working on different repos at the
    same time, not the full fleet of 40.
  - Default TTL is 5 minutes. If a second session starts within 5 minutes of
    the first (on the same config version), it hits the warm entry rather than
    writing a new one.
  - Extended 1-hour TTL (available in direct API calls via
    `cache_control: {ttl: "1h"}`) gives a wider window, at 2× write cost per
    token — still profitable after even one hit within the hour.

  Update `.agents/memory/MEMORY.md` index.

- [x] **Step 3 — API caching pattern reference memory (for future direct calls)**

  No workflow currently calls the Anthropic API directly, but when one is
  added, developers need the pattern immediately.

  Create `.agents/memory/reference/anthropic-api-caching.md` documenting:
  - Use `cache_control: {type: ephemeral}` on the system message block for
    5-minute TTL (1.25× write / 0.1× read).
  - Use `cache_control: {type: ephemeral, ttl: "1h"}` for 1-hour TTL
    (2× write / 0.1× read) — right for any workflow job spaced > 5 min apart.
  - Place stable content (system instructions, skill definitions, shared
    context) **before** any dynamic per-request content so the breakpoint
    sits at the end of the stable prefix.
  - Monitor: `usage.cache_read_input_tokens` should grow relative to
    `usage.cache_creation_input_tokens` as the cache warms.
  - Future: once direct API calls exist, consider a cache pre-warm job
    triggered on push to `master` — calls the API with `max_tokens: 0` and
    `cache_control: {ttl: "1h"}` so the first session after a config change
    hits rather than writes.

  Update `.agents/memory/MEMORY.md` index.

- [x] **Step 4 — API workspace consolidation (already confirmed — verify stays true)**

  A cache entry is visible only to API calls made with a key from the **same
  Anthropic workspace** (a named sub-group within your Anthropic Console
  organisation). Two requests using keys from different workspaces never share
  cache, even if they send identical prompts.

  **Current state (confirmed):** "TeamDev Management OÜ" has a single default
  workspace (Environments list is empty). Both developers use Console API keys
  from this organisation. Both developers share the same cache pool — no action
  needed today.

  **Keep true as the team grows:** do not create separate Environments per
  developer or per project unless cache isolation is intentional. Any new API
  key issued for a new caller (GitHub Actions, scripts, new developer) should
  be issued from the same workspace.

## Log

- 2026-05-24 — drafted from codebase audit; awaiting review and approval
- 2026-05-24 — revised per review: added buildSrc to migrate list, removed dependency-audit caching step, corrected concurrency description to 1–2 repos, dropped pre-warm workflow step (pattern preserved in Step 3 memory), clarified per-workspace semantics in Step 4
- 2026-05-24 — added Step 0 after Console Caching dashboard confirmed zero prompt caching in use; workspace confirmed as single default (no Environments), both devs on same org — Step 4 updated to reflect confirmed state
- 2026-05-24 — Step 0 revised: root cause identified — Console API key users get 5-min TTL by default vs 1-hour for subscription users; ENABLE_PROMPT_CACHING_1H=1 is the fix; warning on first launch is one-time approval only
- 2026-05-24 — Step 0 revised again: switched to Console OAuth (not raw API key) to preserve per-developer identity; ENABLE_PROMPT_CACHING_1H=1 still required for Console OAuth users (5-min TTL default applies to all non-subscription auth)
- 2026-05-24 — Steps 1–4 complete: three memory files created, MEMORY.md index updated, workspace consolidation confirmed; Step 0 remains in progress (Console OAuth setup and verification)
- 2026-05-25 — reverted Step 1: removed `cache-hygiene.md` and references — batching guidance was too restrictive for `config` development cadence
