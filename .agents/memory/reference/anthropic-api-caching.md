---
name: anthropic-api-caching
description: Pattern and pricing for adding prompt caching to any direct Anthropic API call.
metadata:
  type: reference
  since: 2026-05-24
---

Use this when adding a direct Anthropic API call (GitHub Actions workflow,
script, or tool) that sends a stable system prompt.

**Add `cache_control` to the system message block:**

```python
system=[{
    "type": "text",
    "text": "<stable system prompt — skill definition, shared instructions, etc.>",
    "cache_control": {"type": "ephemeral", "ttl": "1h"}
}]
```

Use `ttl: "1h"` for any caller whose requests are spaced more than 5 minutes
apart (GitHub Actions jobs, scheduled tasks, skill invocations). Use the
default 5-minute TTL only for tight interactive loops.

**Pricing (input tokens):**

| Operation | Cost multiplier |
|---|---|
| Cache write (5-min TTL) | 1.25× base input price |
| Cache write (1-hour TTL) | 2× base input price |
| Cache read (any TTL) | 0.1× base input price |

A single cache hit within the TTL window recovers the write premium. Multiple
hits within the hour make the 2× write cost negligible.

**Place stable content before dynamic content.** Cache breakpoints apply to
everything *before* the `cache_control` marker. Dynamic per-request content
(user query, file diff, current date) must come after the last breakpoint.

**Monitor hits via the usage object:**
```python
print(response.usage.cache_read_input_tokens)    # 0 on miss, >0 on hit
print(response.usage.cache_creation_input_tokens) # tokens written to cache
```

**Future:** once direct API calls exist in this org, consider a cache pre-warm
job triggered on push to `master` — calls the API with `max_tokens: 0` and
`cache_control: {ttl: "1h"}` so the first session after a config change
hits rather than writes.

Related: [[cache-warm-window]]
