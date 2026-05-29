#!/usr/bin/env bash
#
# PreToolUse hook: block `gh pr create` unless /pre-pr has successfully run
# for the current HEAD. The hook is intentionally unaware of the repository's
# versioning or build system; the /pre-pr skill decides which checks apply.
#
# Input: hook JSON on stdin (tool_name, tool_input.command).
# Exit:  0 to allow, 2 to block (stderr is surfaced to Claude).
#
set -eu

if ! command -v jq >/dev/null 2>&1; then
  cat >&2 <<EOF
'gh pr create' blocked: this hook requires 'jq' to inspect the tool request.

Install jq and retry, then run /pre-pr before creating the PR.
EOF
  exit 2
fi

input=$(cat)
tool=$(printf '%s' "$input" | jq -r '.tool_name // empty')
[ "$tool" != "Bash" ] && exit 0

cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')

# Split the command on shell separators (`;`, `&`, `|` — `&&`/`||` collapse
# to repeated newlines, which is fine) and check each segment. Only block
# when a segment STARTS (after optional whitespace) with `gh pr create`.
# This avoids false positives like `echo "gh pr create"` or test fixtures
# that mention the string, while still catching `cd dir && gh pr create`
# and `cat body | gh pr create`. `tr` is used (not `sed s///`) because
# BSD `sed` on macOS does not interpret `\n` in the replacement string.
if ! printf '%s' "$cmd" \
    | tr ';&|' '\n\n\n' \
    | grep -qE '^[[:space:]]*gh[[:space:]]+pr[[:space:]]+create([[:space:]]|$)'; then
  exit 0
fi

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || exit 0
sentinel="$repo_root/.git/pre-pr.ok"

block() {
  cat >&2
  exit 2
}

if [ ! -f "$sentinel" ]; then
  block <<EOF
'gh pr create' blocked: pre-PR checks have not run on this clone.

Run /pre-pr first. It runs the applicable build/check command, applies the
version gate only when this repository has a root version.gradle.kts, dispatches
the configured reviewers, then writes $sentinel on success.
EOF
fi

sentinel_status=$(awk -F= '/^status=/{print $2}' "$sentinel")
sentinel_sha=$(awk -F= '/^head=/{print $2}' "$sentinel")
head_sha=$(git -C "$repo_root" rev-parse HEAD)

if [ "$sentinel_status" != "PASS" ]; then
  block <<EOF
'gh pr create' blocked: the last /pre-pr run reported status='$sentinel_status'.

Fix the issues and re-run /pre-pr before creating the PR.
Sentinel: $sentinel
EOF
fi

if [ "$sentinel_sha" != "$head_sha" ]; then
  block <<EOF
'gh pr create' blocked: /pre-pr was last run at commit
  $sentinel_sha
but HEAD is now
  $head_sha

Re-run /pre-pr to revalidate the current tree.
EOF
fi

exit 0
