---
slug: cross-agent-skill-best-practices
branch: codex/audit-skills-discoverability
owner: codex
status: draft
started: 2026-05-31
---

## Goal

Bring the repository skills in `.agents/skills/` closer to the shared skills
standard so they are easy to discover and execute across Codex, Claude, and
other compatible agents. Success means a new agent can identify the right skill
from metadata, load a short `SKILL.md`, follow agent-neutral instructions, and
delegate deterministic work to scripts or references where appropriate.

## Context

- Audit source: Claude skill authoring best practices.[^claude-best-practices]
- Current inventory: 16 skills, 16 `SKILL.md` files, and 16
  `agents/openai.yaml` files.
- Good baseline: skill directory names match frontmatter names, names use the
  expected lowercase hyphenated form, all `SKILL.md` files are under the
  500-line guideline, and frontmatter descriptions are under 1024 characters.
- User direction: optimize for compatibility with Codex, Claude, and other AI
  agents that support the skills standard, not for a single agent runtime.

## Findings

1. Some fragile deterministic workflows are still mostly prose instead of
   scripts.
   - `check-links` embeds site detection, binary preflight, Lychee download,
     Hugo server lifecycle, reporting, and sentinel writing in `SKILL.md`.
   - `dependency-update` asks the agent to parse Kotlin dependency files,
     discover versions, compare versions, and edit files manually.
   - Best-practice risk: high-cognitive-load procedures are harder for agents
     to pick up reliably and should be moved behind deterministic entrypoints
     where practical.

2. `raise-coverage` has a high-impact automatic path.
   - The skill silently installs Kover when no coverage plugin is present.
   - Best-practice risk: a request to add tests can mutate build configuration
     without an explicit approval checkpoint.
   - Cross-agent concern: different agents may interpret "silent install"
     differently, so this should become an explicit policy decision.

3. Long reference files need top-level contents.
   - `raise-coverage/references/coverage-signals.md` is 181 lines.
   - `raise-coverage/references/migrate-to-kover.md` is 352 lines.
   - `gradle-review/practices/tasks.md` is 147 lines.
   - Best-practice risk: reference material over 100 lines should be easier to
     skim before an agent loads or follows a specific section.

4. Some metadata and prompt surfaces are less portable than the rest.
   - `raise-coverage/agents/openai.yaml` has a much longer `default_prompt`
     than other skills.
   - `writer/agents/openai.yaml` does not mention `$writer`, unlike the other
     skill prompts.
   - `raise-coverage/SKILL.md` still uses slash-command phrasing such as
     `/raise-coverage` and `/version-bumped`, which is less portable across
     agents.

5. Evaluation evidence is missing.
   - No eval or scenario files were found under `.agents/skills/`.
   - Only `update-copyright` currently has script tests.
   - Best-practice risk: the repo does not make it visible that skills were
     tested on realistic examples, so future agents cannot distinguish
     validated workflows from untried instructions.

## Plan

- [ ] Decide whether `raise-coverage` may silently install Kover, or whether all
  build-configuration edits require explicit approval.
- [ ] Extract or introduce deterministic entrypoints for the highest-risk
  procedural skills, starting with `check-links` and `dependency-update`.
- [ ] Add table-of-contents sections to reference files over 100 lines.
- [ ] Normalize cross-agent phrasing by removing slash-command assumptions and
  keeping instructions skill-name based.
- [ ] Shorten unusually long `openai.yaml` default prompts while preserving
  discoverability for Codex.
- [ ] Decide whether to add lightweight skill scenarios or eval notes for the
  major skills.
- [ ] Re-audit all skills against the Claude best-practices checklist and record
  the result in this task log.

## Open Decisions

- Should `raise-coverage` require approval before any Kover installation, even
  when no coverage plugin exists?
- Should `dependency-update` get a real implementation script now, or should the
  first pass only split parsing/versioning rules into references?
- What is the desired minimum evaluation artifact: short scenario files,
  executable tests, or both?

## Log

- 2026-05-31: Drafted from the cross-agent skills best-practices audit. Awaiting
  maintainer review before changes.

[^claude-best-practices]: https://platform.claude.com/docs/en/agents-and-tools/agent-skills/best-practices
