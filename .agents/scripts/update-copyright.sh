#!/usr/bin/env bash
#
# PostToolUse hook: refresh the copyright header of source files touched by
# Edit/Write/MultiEdit. Delegates to
# .agents/skills/update-copyright/scripts/update_copyright.py, which:
#   - operates only on recognized source extensions,
#   - never adds a header to a file that does not already have one,
#   - rewrites `today.year` to the current year per the IntelliJ profile.
#
# Input: hook JSON on stdin. Claude Code passes `tool_input.file_path`;
# Codex `apply_patch` passes the patch text in `tool_input.command`.
# Exit:  0 always (post-tool-use; never block).
#
set -u

# Required tools — silently no-op if either is missing so the hook never blocks.
command -v jq >/dev/null 2>&1 || exit 0
command -v python3 >/dev/null 2>&1 || exit 0

input=$(cat)
file=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty' 2>/dev/null || true)
command=$(printf '%s' "$input" | jq -r '.tool_input.command // empty' 2>/dev/null || true)

root="${CLAUDE_PROJECT_DIR:-$(pwd)}"
script="$root/.agents/skills/update-copyright/scripts/update_copyright.py"

[ -f "$script" ] || exit 0

update_path() {
  local path="$1"
  [ -z "$path" ] && return 0
  [ ! -f "$path" ] && return 0
  python3 "$script" --root "$root" "$path" >/dev/null 2>&1 || true
}

if [ -n "$file" ]; then
  update_path "$file"
  exit 0
fi

printf '%s\n' "$command" \
    | sed -nE 's/^\*\*\* (Add|Update) File: (.*)$/\2/p' \
    | sort -u \
    | while IFS= read -r path; do
        update_path "$path"
      done

exit 0
