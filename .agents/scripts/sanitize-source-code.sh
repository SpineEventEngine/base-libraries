#!/usr/bin/env bash
#
# PostToolUse hook: enforce the source-code formatting rules from
# .agents/coding-guidelines.md after Edit/Write/MultiEdit:
#   - strip trailing whitespace
#   - replace 2+ consecutive blank lines with a single blank line
#
# Input: hook JSON on stdin. Claude Code passes `tool_input.file_path`;
# Codex `apply_patch` passes the patch text in `tool_input.command`.
# Exit:  0 always (post-tool-use; never block).
#
set -eu

command -v jq >/dev/null 2>&1 || exit 0

input=$(cat)
file=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')
command=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')

sanitize_file() {
  local path="$1"

  [ -z "$path" ] && return 0
  [ ! -f "$path" ] && return 0

  case "$path" in
    *.java|*.kt|*.kts) ;;
    *) return 0 ;;
  esac

  tmp=$(mktemp)
  awk '
    { sub(/[ \t]+$/, "") }
    /^$/ { blank++; if (blank > 1) next; print; next }
    { blank = 0; print }
  ' "$path" > "$tmp" && mv "$tmp" "$path"
}

if [ -n "$file" ]; then
  sanitize_file "$file"
  exit 0
fi

printf '%s\n' "$command" \
    | sed -nE 's/^\*\*\* (Add|Update) File: (.*)$/\2/p' \
    | sort -u \
    | while IFS= read -r path; do
        sanitize_file "$path"
      done
