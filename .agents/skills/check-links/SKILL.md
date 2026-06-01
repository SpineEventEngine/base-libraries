---
name: check-links
description: >
  Validate the Hugo documentation site under `docs/` or `site/` for broken
  links. Builds the site, starts the Hugo server locally, runs Lychee against
  the rendered HTML using the repo's `lychee.toml`, and reports any broken URLs
  grouped by source Markdown page. Use locally before pushing changes that
  touch `docs/**` or `site/**`, when CI's `Check Links` job fails, or whenever
  the user asks to "check doc links". If no Hugo site exists under `docs/` or
  `site/`, report the check as not applicable instead of failing. Read-only
  with respect to the project sources. Does **not** cover Javadoc/KDoc (out of
  scope for this skill).
---

# Check links in the Hugo docs (repo-specific)

You are the documentation link checker for this Spine Event Engine project.
You build the site under `docs/` or `site/` (auto-detected; see step 0), serve
it locally on port `1414`, run Lychee against the rendered HTML, and report
broken URLs. You mirror what the `.github/workflows/check-links.yml` workflow
does in CI: same Hugo version, same Lychee version, same Hugo environment
(`development`), and the same `lychee.toml`. Two deliberate differences remain:
the skill serves on port `1414` (CI uses `1313`) to avoid clashing with a
developer's local `hugo server`, and the skill writes a local sentinel that CI
does not. Both differences are harmless because `--base-url` is rewritten to
match the local port and the sentinel is consumed only by the local `pre-pr`
skill.

### Pinned versions

`.github/workflows/check-links.yml` is the **single source of truth** for the
Hugo and Lychee pins. This file does not duplicate the current values
because duplicates inevitably drift; see the workflow's `env:` block for
the canonical `HUGO_VERSION` and `LYCHEE_VERSION_TAG`. The auto-download
step (§2) reads `LYCHEE_VERSION_TAG` out of the workflow at runtime, so a
workflow bump propagates automatically. Hugo is not auto-installed; the
skill uses whichever `hugo` is on `$PATH` and only warns (does not block)
if the installed version is older than the workflow's `HUGO_VERSION` —
Hugo's HTML output is stable enough across minor versions that a small
skew does not invalidate link-check results.

The authoritative shared config is `lychee.toml` at the repo root. Do not
fork its exclude list — fix the source link or, if the failing URL is a known
flaky external endpoint, add it to `lychee.toml` once (the change applies to
both the skill and CI).

## When to run

- Any change touches `docs/**` or `site/**` (including reference links,
  `embed-code` blocks, sidenav YAML files, content under `<SITE_DIR>/content/`).
- A change touches `lychee.toml` itself.
- CI reported broken links and you want a fast local repro.
- The user asks to "check the doc links" or invokes the `check-links` skill.

If none of the above is true, decline with a one-line note rather than
running the (~30 s) build+check.

If the repository has no Hugo config under `docs/` or `site/`, return
`APPROVE — no Hugo documentation site found under docs/ or site/.` and stop.
Do not write a `FAIL` sentinel for this not-applicable case.

## Tooling

The skill needs four binaries:

| Tool   | Purpose                                  | Install hint                  |
|--------|------------------------------------------|-------------------------------|
| Hugo   | Build and serve the site                 | `brew install hugo` (extended)|
| Node   | Hugo theme dependencies (`npm ci`)       | `brew install node`           |
| npm    | Same                                     | bundled with Node             |
| Lychee | Link checker                             | `brew install lychee`         |

For **Lychee**, prefer a pre-installed binary on `$PATH`. If none is found,
download the pinned release (see `LYCHEE_VERSION_TAG` in
`.github/workflows/check-links.yml` — the dynamic-read pattern in step 2 below
keeps this version in lock-step with CI) into
`.agents/skills/check-links/.cache/lychee/` and use that path. The pinned
version matches what the CI workflow uses, so behavior is identical.

`.agents/skills/check-links/.cache/` is git-ignored (see `.gitignore`).

## Procedure

Execute the steps in order. On the first failure, stop, write a `FAIL`
sentinel (step 8), and report the failure with the next action.

### 0. Detect site root and work directory

Before any other step, determine `SITE_DIR` (the Hugo site root) and `WORK_DIR`
(the directory where `npm ci` / `hugo` commands run — mirrors `.github/workflows/check-links.yml`):

```bash
SITE_DIR=""
for dir in docs site; do
  for cfg in hugo.toml hugo.yaml \
             config/hugo.toml config/hugo.yaml \
             config/_default/hugo.toml config/_default/hugo.yaml; do
    if [ -f "$dir/$cfg" ]; then
      SITE_DIR="$dir"
      break 2
    fi
  done
done
if [ -z "$SITE_DIR" ]; then
  echo "APPROVE — no Hugo documentation site found under docs/ or site/."
  exit 0
fi

if [ -f "${SITE_DIR}/_preview/package-lock.json" ]; then
  WORK_DIR="${SITE_DIR}/_preview"
elif [ -f "${SITE_DIR}/package-lock.json" ]; then
  WORK_DIR="${SITE_DIR}"
else
  echo "ERROR: No package-lock.json found under ${SITE_DIR}/_preview/ or ${SITE_DIR}/." >&2
  exit 1
fi
```

Use `$SITE_DIR` for content paths and `$WORK_DIR` for build/serve operations in the steps below.

### 1. Scope check

Run `git diff <base>...HEAD --name-only` (default `<base>` = `master` unless
the user provides another). If the change set has **no** files under
`$SITE_DIR/**` and no changes to `lychee.toml`, and the user did not
explicitly ask, decline and exit cleanly.

### 2. Preflight binaries

- `hugo version` → must succeed; capture the version. If missing, stop with
  Must-fix: "Install Hugo extended (`brew install hugo`)." If installed but
  older than the workflow's `HUGO_VERSION` (parse with
  `grep -E '^[[:space:]]+HUGO_VERSION:' .github/workflows/check-links.yml | sed -E 's/.*: *"?([^"]+)"?$/\1/'`), warn but
  continue.
- `node -v` and `npm -v` → must succeed. If missing, stop with Must-fix:
  "Install Node (`brew install node`) at the major version pinned by
  `node-version:` in `.github/workflows/check-links.yml`."
- `lychee --version` → if it succeeds, record the path and version.
- If `lychee` is missing:
  1. Read the canonical pin from the workflow file so the skill cannot drift
     from CI:
     ```bash
     LYCHEE_VERSION_TAG=$(
       grep -E '^[[:space:]]+LYCHEE_VERSION_TAG:' .github/workflows/check-links.yml \
         | sed -E 's/.*: *"?([^"]+)"?$/\1/'
     )
     ```
     Expected shape: `lychee-vX.Y.Z` (the leading `lychee-` is part of the
     upstream release tag, not a typo).
  2. Determine platform via `uname -s` / `uname -m`. Map to the matching
     Lychee asset (recent releases — `v0.24.2` and later — drop the
     version from the asset filename):
     - `Darwin` + `arm64`   → `lychee-aarch64-apple-darwin.tar.gz`
     - `Darwin` + `x86_64`  → `lychee-x86_64-apple-darwin.tar.gz`
     - `Linux`  + `x86_64`  → `lychee-x86_64-unknown-linux-gnu.tar.gz`
     - `Linux`  + `aarch64` → `lychee-aarch64-unknown-linux-gnu.tar.gz`
     - any other combination (e.g. Windows, FreeBSD, 32-bit) → stop with
       Must-fix: "Unsupported platform for Lychee auto-download — install
       Lychee manually (`brew install lychee` / `cargo install lychee`)
       and rerun."
  3. Ensure the cache directory exists *before* the download —
     `mkdir -p .agents/skills/check-links/.cache/lychee/` —
     because the path is git-ignored and absent on a fresh clone,
     and `tar -xzf … -C <dir>` will fail with "no such file or
     directory" if the target does not exist yet. This mirrors the
     `mkdir -p lychee` that `check-links.yml` does before its own
     extract step.
  4. Download from
     `https://github.com/lycheeverse/lychee/releases/download/${LYCHEE_VERSION_TAG}/<asset>`
     into `.agents/skills/check-links/.cache/lychee/` and extract
     with `tar -xzf <asset> --strip-components=1 -C .agents/skills/check-links/.cache/lychee/`
     so the binary lands at
     `.agents/skills/check-links/.cache/lychee/lychee`.
  5. Use `.agents/skills/check-links/.cache/lychee/lychee` for the rest of this run.
  6. Print a one-line note: "Using auto-downloaded Lychee. For faster runs,
     install with `brew install lychee`."

### 3. Install Hugo deps

Run `( cd ${WORK_DIR} && npm ci )`. We deliberately use `npm ci`
(matching the CI workflow's `Install Dependencies` step in `check-links.yml`)
rather than `npm install`:

- `npm ci` installs exactly the versions pinned by `package-lock.json`;
  `npm install` is allowed to update the lockfile and may resolve to
  different transitive versions than CI, which defeats the "render
  identical HTML to CI" goal.
- If `package.json` and `package-lock.json` drift out of sync, `npm ci`
  fails fast with a clear error rather than silently healing the
  lockfile — a divergence we want to surface, not paper over.

### 4. Build the site

Run `( cd ${WORK_DIR} && hugo -e development )`.
This emits `${WORK_DIR}/public/**/*.html`. The `-e development` flag matches
what CI uses in `check-links.yml` so the two builds render identical HTML.
(The helper `${SITE_DIR}/_script/hugo-build` exists for interactive use but
defaults to `production`; we invoke `hugo` directly to keep the env in
lock-step with CI.)

### 5. Start the Hugo server in the background

The server must survive across multiple shell/tool calls (steps 5 → 6 → 8
typically run in separate shells), so we rely on `nohup` alone — a `trap …
EXIT` would fire when *this* shell exits and kill the server before Lychee
can query it. Teardown happens explicitly in step 8.

Before launching, kill any leftover server from a previous crashed run so a
stale process does not hold port `1414`:

```bash
pkill -F /tmp/check-links.hugo.pid 2>/dev/null || true
rm -f /tmp/check-links.hugo.pid

( cd ${WORK_DIR} && nohup hugo server --environment development --port 1414 \
    > /tmp/check-links.hugo.out 2>&1 & echo $! > /tmp/check-links.hugo.pid )
sleep 5

# Verify the captured PID is alive before relying on it. `$!` for
# `nohup foo &` is reliable on bash but not portable across shells; the
# pgrep check turns a silent "Lychee fetches an empty port" failure into
# a clear error.
if ! pgrep -F /tmp/check-links.hugo.pid > /dev/null 2>&1; then
  echo "ERROR: Hugo server failed to start. Tail of log:" >&2
  tail -20 /tmp/check-links.hugo.out >&2 || true
  exit 1
fi
```

Port `1414` is chosen to avoid clashing with a developer's local `hugo server`
(default `1313`). The `--environment development` flag matches CI's build env.

### 6. Run Lychee

```bash
<lychee-path> --config lychee.toml --timeout 60 \
  --base-url http://localhost:1414/ \
  "${WORK_DIR}/public/**/*.html"
```

Capture exit code. Any non-zero exit means at least one broken link.

### 7. Report

Group the broken URLs from Lychee's output by source page. To reverse-map
an HTML path to its Markdown source:

`${WORK_DIR}/public/docs/<section>/<page>/index.html`
↔ `${SITE_DIR}/content/docs/<section>/<page>.md` (or `<page>/_index.md`).

Report in this shape:

```
## Doc link check (<branch> vs <base>)

Hugo: <version>
Lychee: <version> (<path>)
Pages scanned: <N>
Broken URLs: <K>

### <SITE_DIR>/content/docs/<...>/<page>.md
- <broken URL> — <Lychee reason / HTTP status>
- <broken URL> — ...

### <SITE_DIR>/content/docs/<...>/<other-page>.md
- ...
```

If `K == 0`, report a single line: "All links OK."

### 8. Tear down and sentinel

- Kill the Hugo server (and clean up its pid file):

  ```bash
  pkill -F /tmp/check-links.hugo.pid 2>/dev/null || true
  rm -f /tmp/check-links.hugo.pid /tmp/check-links.hugo.out
  ```

  Run this even if Lychee failed — leaving a server on port `1414` would
  poison the next invocation.
- Write `.git/check-links.ok` at the repo root:

  ```
  head=<full HEAD SHA>
  branch=<current branch>
  status=PASS|FAIL
  timestamp=<ISO-8601 UTC>
  hugo=<version>
  lychee=<version>
  pages=<N>
  broken=<K>
  ```

The sentinel is consumed by the `pre-pr` skill's reviewer step: when it
sees a sentinel whose `head=` matches the current HEAD SHA and
`status=PASS`, it skips re-dispatching `check-links` and records it
as APPROVE with the note "cached from `.git/check-links.ok`". Any
HEAD advance (commit, amend, rebase) invalidates the cache automatically.

## Notes

- This skill does **not** modify tracked sources. It does, however, write
  several git-ignored build artifacts during a run — listed here so a future
  reader does not mistake them for unrelated side-effects:
  - `.agents/skills/check-links/.cache/lychee/` — auto-downloaded
    Lychee binary, when the system Lychee was unavailable.
  - `${WORK_DIR}/node_modules/` — installed by `npm ci` in step 3.
  - `${WORK_DIR}/public/` — Hugo's rendered HTML (the corpus Lychee scans).
  - `${WORK_DIR}/resources/` — Hugo's asset-pipeline cache.
  - `.lycheecache` at the repo root — Lychee's per-URL result cache
    (honoured for `max_cache_age = "3d"` per `lychee.toml`).
  - `/tmp/check-links.hugo.{pid,out}` — server PID file and log, both
    removed in step 8's teardown.

  Every path above is matched by an existing `.gitignore` entry; none is
  committed.
- The `lychee.toml` exclude list is the single source of truth for flaky
  external endpoints. If a real link must be excluded, add it there and
  explain why in a comment so CI and local runs stay in sync.
- The skill assumes the docs build succeeds. A Hugo build error is treated
  the same as a link failure — surface it and stop.
- The `include_verbatim = false` setting in `lychee.toml` skips links inside
  code blocks. That is intentional today; flip it on if you specifically need
  to validate examples.

## Related skills

- `review-docs` — prose, KDoc/Javadoc, and Markdown style review. Runs in
  parallel with `check-links` when invoked by `pre-pr`.
- `pre-pr` — composes the above and gates `gh pr create`.
