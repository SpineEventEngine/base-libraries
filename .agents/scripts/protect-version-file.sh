#!/usr/bin/env bash
#
# PreToolUse hook: block direct Edit/Write/MultiEdit on `version.gradle.kts`.
# In repositories that have this file, the bump-version skill owns the
# version-bump policy (snapshot numbering, rebuilds, dependency-report updates,
# conflict resolution). Repositories without it must not add it just to satisfy
# hooks or reviewers.
#
# Input: hook JSON on stdin. Claude edit tools pass `tool_input.file_path`;
# Codex `apply_patch` passes the patch text in `tool_input.command`.
# Exit:  0 to allow, 2 to block with stderr message surfaced to the agent.
#
set -eu

input=$(cat)
file=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')
command=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')

touches_version_file() {
  if [ "$file" = "version.gradle.kts" ] || [ "${file%/version.gradle.kts}" != "$file" ]; then
    return 0
  fi

  printf '%s\n' "$command" \
      | grep -qE '^\*\*\* (Add|Update|Delete) File: (.+/)?version\.gradle\.kts$'
}

if touches_version_file; then
    cat >&2 <<'EOF'
Direct edits to version.gradle.kts are blocked by a project hook.

If this repository already has a root version.gradle.kts, use the bump-version
skill instead:
  /bump-version [snapshot|minor|major]

If this repository does not have a root version.gradle.kts, do not add one just
to satisfy /pre-pr; the version check is not applicable.

See:
  - .agents/version-policy.md
  - .agents/skills/bump-version/SKILL.md
EOF
    exit 2
fi

exit 0
