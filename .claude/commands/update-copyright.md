---
description: Refresh copyright headers from the IntelliJ profile, replacing today.year with the current year.
argument-hint: "[paths...]"
allowed-tools: Bash(python3 .agents/skills/update-copyright/scripts/update_copyright.py:*), Read
---

Follow the `update-copyright` skill exactly:

- Skill: `.agents/skills/update-copyright/SKILL.md`
- Run: `python3 .agents/skills/update-copyright/scripts/update_copyright.py $ARGUMENTS`
- If $ARGUMENTS is empty, run once with `--dry-run`, show the output to the user, then run without `--dry-run`.
- Never add a header to a file that doesn't already have one.
