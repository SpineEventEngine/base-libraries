---
name: cache-warm-window
description: How prompt cache entries are shared between sibling-repo sessions and how to maximise overlap.
metadata:
  type: reference
  since: 2026-05-24
---

Claude Code sessions share a prompt cache entry when they send byte-identical
content within the cache TTL window. Because `migrate` copies `CLAUDE.md` and
`.agents/` verbatim, any two sessions on the same config version share the
same cache slot — provided they fall within the TTL.

**TTL in effect for Console OAuth users:**
- Default: **5 minutes** (applies to all non-subscription auth)
- With `ENABLE_PROMPT_CACHING_1H=1` in `~/.claude/settings.json`: **1 hour**

Developers must have `ENABLE_PROMPT_CACHING_1H=1` set, otherwise the
window is too short for cross-session hits to occur reliably.
This setting will work ONLY for Claude Code which runs the CLI binary.
It will not work for JetBrains Air or any other IDE plugin which does not
run the Claude Code CLI binary.

**Cache is per Anthropic workspace.** All developers authenticated via the
same Anthropic organisation Console org share the same cache pool. Do not
create separate Console workspaces per developer — that would isolate their
cache entries.

**Practical impact:** Realistic concurrency is 1–2 sessions at a time. The
first session after a config change pays the cache-write cost; any session
starting within the next hour (with 1H TTL) reads from cache at 0.1× cost.

Related: [[anthropic-api-caching]]
