---
name: update-copyright
description: >
  Update source file copyright headers from the IntelliJ IDEA copyright profile,
  replacing `today.year` with the current year.
  Automatically apply when source files are modified in a change set.
---

# Copyright Update

**Command:** `python3 .agents/skills/update-copyright/scripts/update_copyright.py`

1. Scope: explicit files/dirs from the user, or all tracked source files if none given.
2. No explicit paths → run with `--dry-run` first, then without.
3. Relay stdout (notice source, file count, changed paths) to the user.
4. Never add a copyright header to a file that does not already have one.
