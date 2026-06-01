---
name: update-copyright
description: >
  Update source file copyright headers from the IntelliJ IDEA copyright profile,
  replacing `today.year` with the current year.
  Automatically apply to changed source files when source files are modified
  in a change set.
---

# Copyright Update

**Command:** `python3 .agents/skills/update-copyright/scripts/update_copyright.py`

1. Scope:
   - Automatic follow-up after edits: collect the source files modified by the
     current change set and pass those paths explicitly. Do not run the command
     without paths in the automatic path. If no changed source files remain
     after filtering, skip the command.
   - User-provided files/dirs: pass the requested paths explicitly.
   - Repo-wide refresh: use no explicit paths only when the user directly asks
     to update all tracked source files.
2. Repo-wide refresh → run with `--dry-run` first, then without.
3. Relay stdout (notice source, file count, changed paths) to the user.
4. Never add a copyright header to a file that does not already have one.
