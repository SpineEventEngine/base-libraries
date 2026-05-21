#!/usr/bin/env bash
#
# PreToolUse hook: block any `./gradlew` invocation that could publish to
# Maven Local without a version bump on the current branch. Wraps the
# Layer-1 deterministic check at `version-bumped.sh`.
#
# This is intentionally broad: it fires on `build`, `publish`,
# `publishToMavenLocal`, and any `:publish*` task. Many repos in this
# constellation chain `publishToMavenLocal` into `build` because
# integration tests consume those local artifacts, so `build` itself is
# publish-risky. False positives (blocking a pure compile) are preferable
# to overwriting a previously published snapshot that consuming repos
# rely on.
#
# Input: hook JSON on stdin (tool_name, tool_input.command).
# Exit:  0 to allow, 2 to block (stderr is surfaced to Claude).
#
set -eu

input=$(cat)
tool=$(printf '%s' "$input" | jq -r '.tool_name // empty')
[ "$tool" != "Bash" ] && exit 0

cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')

# Split the command on shell separators (`;`, `&`, `|`) and inspect each
# segment. Only block when a segment, after optional whitespace, invokes
# `./gradlew` (or `./config/gradlew`) with a publish-risky task. Avoids
# false positives on `echo "./gradlew build"` or fixtures.
risky_segment() {
  local seg="$1"
  # Must start with a gradlew invocation.
  printf '%s' "$seg" | grep -qE '^[[:space:]]*\.?/?(config/)?gradlew([[:space:]]|$)' || return 1
  # Must mention a publish-risky task. `build` is risky because it can
  # finalize publishToMavenLocal in this config. The leading
  # `(:[A-Za-z0-9_.-]+)*:?` covers qualified task paths
  # (e.g. `:module:build`, `:a:b:publishToMavenLocal`) and a single
  # leading-colon form (`:publishMavenJavaPublicationToMavenLocal`).
  # `publish[^[:space:]]*` then catches every publish-task variant.
  printf '%s' "$seg" | grep -qE '(^|[[:space:]])(:[A-Za-z0-9_.-]+)*:?(build|publish[^[:space:]]*|publishToMavenLocal|publishAllPublicationsToMavenLocal)([[:space:]]|$)'
}

block_needed=0
# `|| [ -n "$segment" ]` makes the loop process the final segment when the
# input has no trailing newline (which is the case for `printf '%s'`).
while IFS= read -r segment || [ -n "$segment" ]; do
  if risky_segment "$segment"; then
    block_needed=1
    break
  fi
done < <(printf '%s' "$cmd" | tr ';&|' '\n\n\n')

[ "$block_needed" -eq 0 ] && exit 0

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || exit 0
script="$repo_root/.agents/skills/version-bumped/scripts/version-bumped.sh"

# If the helper is missing (e.g. partial clone), don't pretend we gated.
if [ ! -x "$script" ]; then
  exit 0
fi

# `&& rc=0 || rc=$?` captures the exit code regardless of success/failure.
# After `if cmd; then ... fi`, $? reflects the if-fi structural exit (0),
# not the failed test's exit code — so we cannot use the if-fi form here.
err_file="/tmp/version-bumped.$$.err"
VERSION_BUMPED_QUIET=1 "$script" 2>"$err_file" && rc=0 || rc=$?
if [ "$rc" -eq 0 ]; then
  rm -f "$err_file"
  exit 0
fi
err_payload=$(cat "$err_file" 2>/dev/null || true)
rm -f "$err_file"

# Layer-1 returned a configuration error — do not block, surface the note.
if [ "$rc" -ne 1 ]; then
  printf '%s\n' "$err_payload" >&2
  exit 0
fi

cat >&2 <<EOF
'./gradlew' blocked: branch differs from the base ref but
version.gradle.kts is not bumped. Publishing would overwrite the Maven
Local artifact at the base version, which integration tests in consumer
repos may rely on.

Run /version-bumped to auto-recover (it invokes /bump-version and re-runs
the check), or /bump-version directly.

Underlying check (.agents/skills/version-bumped/scripts/version-bumped.sh) reported:
$err_payload
EOF
exit 2
