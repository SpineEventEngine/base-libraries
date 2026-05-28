#!/usr/bin/env bash
#
# Verifies that a feature branch which differs from the base ref also
# bumps `version.gradle.kts` strictly above the base version. Mirrors the
# universal "every branch advances the version" policy: a branch with any
# changes is a candidate for publishing — sometimes the only change is the
# bump itself, used to retry a publish that failed because Maven
# repositories were overloaded.
#
# Exit codes:
#   0 — OK: repo has no root `version.gradle.kts`, OR branch has no diff
#       vs base, OR working-tree version is strictly greater than base
#       version.
#   1 — Block: branch differs from base but version is unchanged or
#       decreased. Stderr points to `/bump-version`.
#   2 — Configuration error (bad base ref, parse failure). Stderr explains.
#
# Inputs (env, all optional):
#   VERSION_BUMPED_BASE   Base ref to compare against. Default: master,
#                         then main if master is absent.
#   VERSION_BUMPED_KEY    Name of the `extra` property holding the
#                         publishing version (e.g. `versionToPublish`,
#                         `validationVersion`, `bootstrapVersion`). When
#                         set, bypasses auto-discovery. Useful for repos
#                         that don't follow the `version = extra["…"]`
#                         pattern in `build.gradle.kts`.
#   VERSION_BUMPED_QUIET  When `1`, suppress the "OK" line on stdout.
#                         The publish-version-gate hook sets this.
#
# Publishing-key discovery:
#   The publishing version's variable name varies across Spine repos
#   (`versionToPublish`, `validationVersion`, `compilerVersion`, …).
#   `version.gradle.kts` may also declare other `val xxxVersion by extra(...)` entries
#   that are *dependency* versions of other Spine modules — not this
#   project's own publishing version — so the key cannot be picked by
#   inspecting `version.gradle.kts` alone.
#
#   The canonical source is `build.gradle.kts`, which assigns
#   `version = extra["KEY"]!!`. This script scans for that pattern,
#   picks the unique key, and parses its value from `version.gradle.kts`.
#   If `build.gradle.kts` does not contain such a line, the script falls
#   back to `versionToPublish`. Set `VERSION_BUMPED_KEY` to override.
#
# Notes:
#   * Companion to the Gradle task `checkVersionIncrement` (see
#     `buildSrc/.../publish/CheckVersionIncrement.kt`). The Gradle task
#     asks "is this version already in remote Maven metadata?" — this
#     script asks the simpler local question "has this branch advanced
#     the version vs base?". The two checks are complementary; neither
#     subsumes the other.
#   * The working tree is included in the change-detection so the gate
#     reflects what `./gradlew build` would actually publish.
#
set -eu

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || {
  echo "version-bumped: not inside a git repository" >&2
  exit 2
}
cd "$repo_root"

version_file="version.gradle.kts"

# --- N/A: not a versioned project ----------------------------------------
if [ ! -f "$version_file" ]; then
  [ "${VERSION_BUMPED_QUIET:-0}" = "1" ] || echo "version-bumped: N/A (no root version.gradle.kts)"
  exit 0
fi

# --- Resolve base ref ----------------------------------------------------
base="${VERSION_BUMPED_BASE:-}"
if [ -z "$base" ]; then
  if git show-ref --verify --quiet refs/heads/master; then
    base=master
  elif git show-ref --verify --quiet refs/heads/main; then
    base=main
  else
    echo "version-bumped: no master or main branch found; set VERSION_BUMPED_BASE" >&2
    exit 2
  fi
fi

if ! git rev-parse --verify --quiet "$base" >/dev/null; then
  echo "version-bumped: base ref '$base' does not resolve" >&2
  exit 2
fi

# When we are on the base branch itself, there is nothing to gate.
current_branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [ "$current_branch" = "$base" ] || [ "$current_branch" = "${base##*/}" ]; then
  [ "${VERSION_BUMPED_QUIET:-0}" = "1" ] || echo "version-bumped: on base branch ($current_branch); nothing to gate"
  exit 0
fi

merge_base=$(git merge-base HEAD "$base" 2>/dev/null) || {
  echo "version-bumped: cannot find merge-base of HEAD and '$base'" >&2
  exit 2
}

# --- Detect any branch divergence vs base (committed/worktree/untracked) -
committed=$(git diff --name-only "$merge_base"..HEAD 2>/dev/null || true)
worktree=$(git diff --name-only HEAD 2>/dev/null || true)
untracked=$(git ls-files --others --exclude-standard 2>/dev/null || true)

if [ -z "$committed" ] && [ -z "$worktree" ] && [ -z "$untracked" ]; then
  [ "${VERSION_BUMPED_QUIET:-0}" = "1" ] || echo "version-bumped: no changes vs $base"
  exit 0
fi

# --- Discover the publishing-version key ---------------------------------
# Source of truth is `build.gradle.kts` (or `build.gradle`). Two shapes are
# recognised, in order:
#
#   a) version = extra["KEY"]
#   b) version = IDENTIFIER         (with `val IDENTIFIER ... by extra` nearby)
#
# Single or double quotes are accepted in shape (a). If multiple distinct
# keys appear across shapes, the script refuses to guess and asks the user
# to set VERSION_BUMPED_KEY.
#
# Return codes:
#   0 — printed a unique key on stdout
#   1 — no candidates found (caller should fall back)
#   2 — ambiguous; diagnostic already on stderr
discover_key() {
  local files keys_a keys_b keys count
  files=""
  [ -f build.gradle.kts ] && files="build.gradle.kts"
  [ -f build.gradle ] && files="$files build.gradle"
  [ -z "$files" ] && return 1
  # Shape (a): version = extra["KEY"]
  # Anchored to start-of-line (modulo leading whitespace) so that comments
  # like `// version = extra["x"]` and identifiers like `fooversion = ...`
  # don't produce false matches.
  # shellcheck disable=SC2086
  keys_a=$(grep -hE '^[[:space:]]*version[[:space:]]*=[[:space:]]*extra[[:space:]]*\[[[:space:]]*["'"'"'][^"'"'"']+["'"'"']' $files 2>/dev/null \
      | sed -nE 's/.*extra[[:space:]]*\[[[:space:]]*["'"'"']([^"'"'"']+)["'"'"'].*/\1/p')
  # Shape (b): version = IDENTIFIER (bare Kotlin identifier, no '[' or '"').
  # Only accept the identifier if the same file also declares
  # `val IDENTIFIER[: String]? by extra` — otherwise it's a plain local
  # variable (common in Groovy `build.gradle`), not an `extra` property we
  # can resolve in `version.gradle.kts`.
  local candidates_b cand
  # shellcheck disable=SC2086
  candidates_b=$(grep -hE '^[[:space:]]*version[[:space:]]*=[[:space:]]*[A-Za-z_][A-Za-z0-9_]*[[:space:]]*$' $files 2>/dev/null \
      | sed -nE 's/^[[:space:]]*version[[:space:]]*=[[:space:]]*([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*$/\1/p')
  keys_b=""
  for cand in $candidates_b; do
    # shellcheck disable=SC2086
    if grep -hE "^[[:space:]]*val[[:space:]]+${cand}([[:space:]]*:[[:space:]]*String)?[[:space:]]+by[[:space:]]+extra([^A-Za-z0-9_]|\$)" $files >/dev/null 2>&1; then
      keys_b="${keys_b}${cand}
"
    fi
  done
  keys=$(printf '%s\n%s' "$keys_a" "$keys_b" | sed '/^$/d' | sort -u)
  [ -z "$keys" ] && return 1
  count=$(printf '%s\n' "$keys" | wc -l | tr -d ' ')
  if [ "$count" -gt 1 ]; then
    {
      echo "version-bumped: ambiguous publishing key in build scripts:"
      while IFS= read -r k; do printf '  %s\n' "$k"; done <<< "$keys"
      echo "  Set VERSION_BUMPED_KEY to disambiguate."
    } >&2
    return 2
  fi
  printf '%s' "$keys"
}

key="${VERSION_BUMPED_KEY:-}"
if [ -z "$key" ]; then
  set +e
  key=$(discover_key)
  rc=$?
  set -e
  if [ "$rc" = "2" ]; then
    exit 2
  fi
  if [ "$rc" != "0" ] || [ -z "$key" ]; then
    key="versionToPublish"
  fi
fi

# --- Parse a `val KEY by extra(...)` from a Gradle file content ----------
# Handles three shapes (per .agents/skills/bump-version/SKILL.md step 2):
#   1. val KEY[: String]? by extra("X")           — literal extra
#   2. val SRC[: String]? by extra("X")           — alias chain via extra
#      val KEY[: String]? by extra(SRC)
#   3. val SRC[: String]? = "X"                   — alias chain via plain val
#      val KEY[: String]? by extra(SRC)
# The key name is parameterized so that any project-specific name works
# (versionToPublish, validationVersion, bootstrapVersion, botVersion, …).
parse_version() {
  local content="$1" name="$2"
  local v varName
  # Shape 1: literal.
  v=$(printf '%s' "$content" \
      | grep -E "val[[:space:]]+${name}([[:space:]]*:[[:space:]]*String)?[[:space:]]+by[[:space:]]+extra\(\"" \
      | head -n1 \
      | sed -nE 's/.*extra\("([^"]+)".*/\1/p')
  if [ -n "$v" ]; then
    printf '%s' "$v"
    return 0
  fi
  # Shapes 2 & 3: extract the alias source identifier.
  varName=$(printf '%s' "$content" \
      | grep -E "val[[:space:]]+${name}([[:space:]]*:[[:space:]]*String)?[[:space:]]+by[[:space:]]+extra\(" \
      | head -n1 \
      | sed -nE 's/.*extra\(([A-Za-z_][A-Za-z0-9_]*)\).*/\1/p')
  if [ -n "$varName" ]; then
    # Shape 2: source is `val SRC ... by extra("X")`.
    v=$(printf '%s' "$content" \
        | grep -E "val[[:space:]]+${varName}([[:space:]]*:[[:space:]]*String)?[[:space:]]+by[[:space:]]+extra\(\"" \
        | head -n1 \
        | sed -nE 's/.*extra\("([^"]+)".*/\1/p')
    if [ -n "$v" ]; then
      printf '%s' "$v"
      return 0
    fi
    # Shape 3: source is `val SRC[: String]? = "X"`.
    v=$(printf '%s' "$content" \
        | grep -E "val[[:space:]]+${varName}([[:space:]]*:[[:space:]]*String)?[[:space:]]*=[[:space:]]*\"" \
        | head -n1 \
        | sed -nE 's/.*=[[:space:]]*"([^"]+)".*/\1/p')
    if [ -n "$v" ]; then
      printf '%s' "$v"
      return 0
    fi
  fi
  return 1
}

head_content=$(cat "$version_file" 2>/dev/null || true)
head_version=$(parse_version "$head_content" "$key" || true)
if [ -z "$head_version" ]; then
  echo "version-bumped: cannot parse '$key' from working-tree $version_file" >&2
  exit 2
fi

# Base content may legitimately not exist (file newly introduced).
base_content=$(git show "$base:$version_file" 2>/dev/null || true)
if [ -z "$base_content" ]; then
  [ "${VERSION_BUMPED_QUIET:-0}" = "1" ] || echo "version-bumped: $version_file newly introduced at $head_version; treating as bumped"
  exit 0
fi

base_version=$(parse_version "$base_content" "$key" || true)
if [ -z "$base_version" ]; then
  echo "version-bumped: cannot parse '$key' from $base:$version_file" >&2
  exit 2
fi

# --- Strict-greater comparison via `sort -V` -----------------------------
if [ "$head_version" = "$base_version" ]; then
  cmp="equal"
elif [ "$(printf '%s\n%s\n' "$base_version" "$head_version" | sort -V | tail -n1)" = "$head_version" ]; then
  cmp="greater"
else
  cmp="lesser"
fi

if [ "$cmp" = "greater" ]; then
  [ "${VERSION_BUMPED_QUIET:-0}" = "1" ] || echo "version-bumped: OK ($key: $base_version -> $head_version)"
  exit 0
fi

cat >&2 <<EOF
version-bumped: BLOCK — branch differs from $base
  but $version_file '$key' is $cmp ($base_version vs $head_version).

  Publishing now would overwrite the Maven Local artifact at
  $base_version, which integration tests in consumer repos may rely on.

  Run /bump-version (or invoke /version-bumped to auto-recover).
  See .agents/version-policy.md and .agents/skills/bump-version/SKILL.md.
EOF
exit 1
