---
description: Write or update Markdown / KDoc documentation per Spine documentation conventions.
argument-hint: "<topic or file path>"
allowed-tools: Read, Edit, Write, Grep, Glob
---

Follow the `writer` skill exactly:

- Skill: `.agents/skills/writer/SKILL.md`
- Topic / target: $ARGUMENTS
- Decide audience first (end user, contributor, maintainer, tooling).
- Prefer updating an existing doc over creating a new one.
- Keep `docs/data/docs/<section>/<version>/sidenav.yml` in sync when adding, removing, moving, or renaming pages under `docs/content/docs/<section>/`.
- Honor `.agents/documentation-guidelines.md` and `.agents/documentation-tasks.md`.
