#!/usr/bin/env bash
#
# Shared helpers for the api-discovery scripts.
# Sourced by ../discover, ../extract-sources, ../clean-cache.
#
# All functions write diagnostics to stderr; "return values" go to stdout.
#
# Conventions:
#   - Bash 3.2 compatible (macOS default).
#   - No external deps beyond coreutils, grep, sed, unzip.
#   - `set -euo pipefail` is set by the caller, not here.

# Exit codes used across the scripts.
readonly EX_OK=0
readonly EX_FAIL=1
readonly EX_NO_CACHE=10        # cache uninitialized; agent runs bootstrap

# Resolve the consumer repository root — the nearest ancestor of $PWD
# containing `buildSrc/src/main/kotlin/io/spine/dependency/`. Falls back to
# CLAUDE_PROJECT_DIR (set by Claude Code) if no such ancestor exists.
# Prints the absolute path to stdout. Exits non-zero if it cannot resolve.
consumer_repo_root() {
    local dir="${1:-$PWD}"
    while [ "$dir" != "/" ] && [ -n "$dir" ]; do
        if [ -d "$dir/buildSrc/src/main/kotlin/io/spine/dependency" ]; then
            printf '%s\n' "$dir"
            return 0
        fi
        dir="$(dirname -- "$dir")"
    done
    if [ -n "${CLAUDE_PROJECT_DIR:-}" ] && \
       [ -d "$CLAUDE_PROJECT_DIR/buildSrc/src/main/kotlin/io/spine/dependency" ]; then
        printf '%s\n' "$CLAUDE_PROJECT_DIR"
        return 0
    fi
    return 1
}

# Resolve the workspace root (parent of the consumer repo by default).
# Honors an optional pointer file at
# `<scripts-dir>/.workspace-root` containing an absolute path; used when the
# user picks "alternative root" during bootstrap.
workspace_root() {
    local scripts_dir="${SPINE_API_DISCOVERY_DIR:-}"
    if [ -z "$scripts_dir" ]; then
        local repo
        repo="$(consumer_repo_root)" || return 1
        scripts_dir="$repo/.agents/scripts/api-discovery"
    fi
    local pointer="$scripts_dir/.workspace-root"
    if [ -f "$pointer" ]; then
        # Read the first line verbatim. `IFS=` keeps internal spaces
        # (paths like `/Users/me/Spine Workspace` must survive intact);
        # `read -r` strips the trailing newline. Strip a stray CR for
        # Windows-style line endings.
        local custom=""
        IFS= read -r custom < "$pointer" 2>/dev/null || true
        custom="${custom%$'\r'}"
        if [ -n "$custom" ] && [ -d "$custom" ]; then
            printf '%s\n' "$custom"
            return 0
        fi
    fi
    local repo
    repo="$(consumer_repo_root)" || return 1
    (cd "$repo/.." && pwd)
}

# Directory that holds the per-workstation api-discovery cache.
cache_root() {
    local ws
    ws="$(workspace_root)" || return 1
    printf '%s/.agents/caches/api-discovery\n' "$ws"
}

# Subdirectory under cache_root where extracted sources live.
sources_root() {
    local root
    root="$(cache_root)" || return 1
    printf '%s/sources\n' "$root"
}

# Returns 0 if the sources cache directory exists; 1 otherwise.
cache_initialized() {
    local s
    s="$(sources_root)" || return 1
    [ -d "$s" ]
}

# Returns the first Gradle-cache JAR path matching the coordinates and
# optional suffix ("-sources" or empty). Empty stdout means "not found".
find_gradle_cache_jar() {
    local group="$1" artifact="$2" version="$3" suffix="${4:-}"
    local base="$HOME/.gradle/caches/modules-2/files-2.1/$group/$artifact/$version"
    [ -d "$base" ] || return 0
    local jar
    jar="$(find "$base" -maxdepth 2 -type f \
              -name "${artifact}-${version}${suffix}.jar" 2>/dev/null \
              | head -n 1)"
    [ -n "$jar" ] && printf '%s\n' "$jar"
    return 0
}

# Extract the canonical `const val version` value from a Spine local/<X>.kt
# file. Anchors at line start (with optional access modifier) so that
# `const val version` strings inside KDoc, comments, or other quoted text
# do not match. Each local/<X>.kt is expected to declare exactly one
# top-level `version` constant; multi-artifact files use different
# constant names (e.g. `mcVersion`) for their non-canonical versions.
read_declared_version() {
    local file="$1"
    [ -f "$file" ] || return 1
    sed -nE 's/^[[:space:]]*(private[[:space:]]+|internal[[:space:]]+|public[[:space:]]+|protected[[:space:]]+)?const[[:space:]]+val[[:space:]]+version[[:space:]]*=[[:space:]]*"([^"]+)".*/\2/p' \
        "$file" | head -n 1
}

# Read a `val <NAME>[: Type] by extra("VALUE")` declaration from a file.
# Prints VALUE on stdout; empty if not found.
_read_extra_val() {
    local file="$1" name="$2"
    sed -nE 's/^[[:space:]]*val[[:space:]]+'"$name"'([[:space:]]*:[[:space:]]*[A-Za-z]+)?[[:space:]]+by[[:space:]]+extra\("([^"]+)"\).*/\2/p' \
        "$file" | head -n 1
}

# Read the sibling's "main" version from `<sibling>/version.gradle.kts`.
# Tries (in order):
#   1. `versionToPublish` — canonical name used by most siblings.
#   2. `<camelCaseLower(basename(sibling))>Version` — e.g. `mcJavaVersion`
#      for sibling `mc-java`, `protoDataVersion` for `ProtoData`.
# Returns non-zero if neither is found; callers treat that as
# "freshness check unavailable".
read_sibling_version() {
    local sibling="$1"
    local file="$sibling/version.gradle.kts"
    [ -f "$file" ] || return 1

    local v
    v="$(_read_extra_val "$file" "versionToPublish")"
    if [ -n "$v" ]; then
        printf '%s\n' "$v"
        return 0
    fi

    local sibling_name camel
    sibling_name="$(basename -- "$sibling")"
    camel="$(camel_case_lower "$sibling_name")Version"
    v="$(_read_extra_val "$file" "$camel")"
    if [ -n "$v" ]; then
        printf '%s\n' "$v"
        return 0
    fi

    return 1
}

# Convert a PascalCase name to kebab-case.
# Examples: Base -> base; CoreJvm -> core-jvm; CoreJvmCompiler -> core-jvm-compiler.
kebab_case() {
    printf '%s\n' "$1" | sed -E 's/([a-z0-9])([A-Z])/\1-\2/g; s/([A-Z]+)([A-Z][a-z])/\1-\2/g' \
        | tr '[:upper:]' '[:lower:]'
}

# Convert a kebab-case or PascalCase name to camelCase (first letter lowercase).
# Examples: base-libraries -> baseLibraries; mc-java -> mcJava;
#          core-jvm-compiler -> coreJvmCompiler; ProtoData -> protoData.
camel_case_lower() {
    local input="$1"
    local pascal
    pascal="$(printf '%s\n' "$input" | awk -F- '{
        out=""
        for (i = 1; i <= NF; i++) {
            out = out toupper(substr($i, 1, 1)) substr($i, 2)
        }
        print out
    }')"
    local first rest
    first="$(printf '%s' "$pascal" | cut -c1 | tr '[:upper:]' '[:lower:]')"
    rest="$(printf '%s' "$pascal" | cut -c2-)"
    printf '%s%s\n' "$first" "$rest"
}

# Given a Spine local/<X>.kt file, deduce its sibling repository name.
# Priority:
#   1. `https://github.com/SpineEventEngine/<NAME>` URL inside the file.
#   2. kebab-case of the file's basename (without `.kt`).
sibling_name_from_dep_file() {
    local file="$1"
    [ -f "$file" ] || return 1
    local from_url
    from_url="$(sed -nE 's|.*github\.com/SpineEventEngine/([A-Za-z0-9._-]+).*|\1|p' \
                "$file" | head -n 1)"
    if [ -n "$from_url" ]; then
        # Trim any trailing slash or punctuation.
        from_url="${from_url%/}"
        printf '%s\n' "$from_url"
        return 0
    fi
    local base
    base="$(basename -- "$file" .kt)"
    kebab_case "$base"
}

# Returns 0 if the given directory contains a Kotlin/Java source set.
# Recognizes plain `src/main` and Kotlin Multiplatform names such as
# `src/commonMain`, `src/jvmMain`, `src/jsMain`, `src/nativeMain`.
has_source_set() {
    local dir="$1"
    [ -d "$dir/src" ] || return 1
    local candidate
    for candidate in main commonMain jvmMain jsMain nativeMain; do
        [ -d "$dir/src/$candidate" ] && return 0
    done
    return 1
}

# Resolve a submodule inside a sibling that owns a given artifact.
# Tries candidate subdirectory names in order, returning the first that
# contains a recognizable source set:
#   1. Sibling root (single-module siblings such as `reflect`, `testlib`).
#   2. `<sibling>/<artifact>`  (artifact name == submodule name).
#   3. `<sibling>/<artifact-with-`spine-`-stripped>`
#      (`spine-base` -> `base-libraries/base`).
#   4. `<sibling>/<artifact-with-`spine-<lowercase-sibling>-`-stripped>`
#      (`spine-protodata-backend` -> `ProtoData/backend`).
#   5. `<sibling>/<sibling-name>`
#      (covers `spine-tool-base` -> `tool-base/tool-base`).
# Falls back to the sibling root when no candidate matches.
resolve_submodule_path() {
    local sibling="$1" artifact="$2"
    [ -d "$sibling" ] || return 1

    if has_source_set "$sibling"; then
        printf '%s\n' "$sibling"
        return 0
    fi

    local sibling_name lower_sibling
    sibling_name="$(basename -- "$sibling")"
    lower_sibling="$(printf '%s' "$sibling_name" | tr '[:upper:]' '[:lower:]')"

    local candidates=()
    candidates+=("$artifact")

    case "$artifact" in
        spine-*) candidates+=("${artifact#spine-}") ;;
    esac

    case "$artifact" in
        spine-${lower_sibling}-*) candidates+=("${artifact#spine-${lower_sibling}-}") ;;
        ${lower_sibling}-*)       candidates+=("${artifact#${lower_sibling}-}") ;;
    esac

    candidates+=("$sibling_name")

    local cand
    for cand in "${candidates[@]}"; do
        [ -n "$cand" ] || continue
        if has_source_set "$sibling/$cand"; then
            printf '%s/%s\n' "$sibling" "$cand"
            return 0
        fi
    done

    printf '%s\n' "$sibling"
}

# Identify whether a Maven group belongs to the Spine sibling ecosystem.
# Returns 0 (true) for Spine groups, 1 (false) otherwise.
is_spine_group() {
    case "$1" in
        io.spine|io.spine.tools|io.spine.protodata|io.spine.validation)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

# Parse a query into group, artifact, version. Sets the globals
# Q_GROUP, Q_ARTIFACT, Q_VERSION. Some may be empty.
# Accepts: `group:artifact:version`, `group:artifact`, `artifact`, or a
# free-form name that we treat as either an artifact or a sibling label.
# Returns 0 always; the caller decides what an empty field means.
parse_query() {
    local q="$1"
    Q_GROUP=""; Q_ARTIFACT=""; Q_VERSION=""
    local rest
    case "$q" in
        *:*:*)
            Q_GROUP="${q%%:*}"
            rest="${q#*:}"
            Q_ARTIFACT="${rest%%:*}"
            Q_VERSION="${rest#*:}"
            ;;
        *:*)
            Q_GROUP="${q%%:*}"
            Q_ARTIFACT="${q#*:}"
            ;;
        *)
            Q_ARTIFACT="$q"
            ;;
    esac
}

# Escape every non-alphanumeric character so the result is safe to embed
# in a POSIX ERE pattern. Cheap overkill — Maven artifact names should
# never need most of these, but the caller's input is untrusted.
escape_ere() {
    printf '%s' "$1" | sed 's/[^A-Za-z0-9]/\\&/g'
}

# Locate the consumer repo's local/<X>.kt file that declares a Maven artifact.
# Some local files build artifact coordinates via Kotlin string templates
# (`"$prefix-base"`, `"$group:$prefix-java:$version"`). To match those we
# expand the per-file `prefix` constant before grepping. Other template
# variables resolve to literals already present in the source, so a plain
# grep finds them.
# Returns the path of the first matching file (or empty).
find_local_dep_file_for_artifact() {
    local artifact="$1"
    local repo
    repo="$(consumer_repo_root)" || return 1
    local local_dir="$repo/buildSrc/src/main/kotlin/io/spine/dependency/local"
    [ -d "$local_dir" ] || return 0

    # Validate the artifact name against the Maven convention before
    # building a regex from it. Reject anything we cannot guarantee is
    # safe; this prevents shell-quoted regex metacharacters in
    # caller-supplied input from being interpreted by `grep -E`.
    case "$artifact" in
        ''|*[!A-Za-z0-9._-]*)
            log_warn "invalid artifact name (allowed: A-Z a-z 0-9 . _ -): $artifact"
            return 1
            ;;
    esac
    local artifact_esc
    artifact_esc="$(escape_ere "$artifact")"

    local file prefix expanded
    for file in "$local_dir"/*.kt; do
        [ -f "$file" ] || continue
        prefix="$(sed -nE 's/.*const[[:space:]]+val[[:space:]]+prefix[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' \
                  "$file" | head -1)"
        if [ -n "$prefix" ]; then
            expanded="$(sed -e 's|\$prefix|'"$prefix"'|g; s|\${prefix}|'"$prefix"'|g' "$file")"
        else
            expanded="$(cat -- "$file")"
        fi
        # Match the artifact as a complete coordinate component:
        # delimited by `"`, `:`, or `-` on either side, never as a substring.
        if printf '%s\n' "$expanded" | grep -qE "[\":-]${artifact_esc}[\":-]|[\":-]${artifact_esc}\$|^${artifact_esc}\$"; then
            printf '%s\n' "$file"
            return 0
        fi
    done
    return 0
}

# Emit a stderr line tagged with the scripts' identity, so the agent can
# distinguish them from unrelated noise.
log_warn() {
    printf 'api-discovery: %s\n' "$*" >&2
}
