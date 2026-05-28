# `api-discovery` scripts

Resolve the on-disk location of a Maven artifact's source code for
agents and developers, without repeatedly `unzip`-ing JARs out of the
Gradle cache.

The agent-facing documentation lives in
[`../../skills/api-discovery/SKILL.md`](../../skills/api-discovery/SKILL.md);
this file is the implementation reference.

## Why

Agents investigating library APIs used to run dozens of `find
~/.gradle/caches` + `unzip -l` + `unzip -p` calls per question. Each
`unzip` decompresses the archive from scratch — slow and token-heavy.

This package replaces that pattern with two cheap reads:

1. **Sibling-first** — every Spine artifact maps to a sibling clone
   under `<workspace-root>/<repo>/`. The source tree is already on
   disk; we just resolve the right submodule path.
2. **Extraction cache** — non-Spine artifacts (Jackson, Guava, etc.)
   have their `-sources.jar` extracted **once** to a per-workstation
   cache. Subsequent queries return instantly.

## Layout

```
.agents/scripts/api-discovery/
├── README.md           # this file
├── lib/common.sh       # shared bash helpers
├── discover            # main entry — resolve a coordinate to a path
├── extract-sources     # one-shot JAR extraction (race-safe)
├── update-sibling      # `git pull --ff-only` a stale sibling (safe-guarded)
└── clean-cache         # prune the extraction cache
```

The cache itself is **not** under the repo. It lives at:

```
<workspace-root>/.agents/caches/api-discovery/sources/<group>/<artifact>/<version>/
```

`<workspace-root>` defaults to the parent of the consumer repo (e.g.
`/Users/<you>/Projects/Spine/` when the consumer repo is
`.../Spine/config/`). To override, write the absolute path to an
alternative root into `.workspace-root` next to this README (the
script is gitignored).

## Bootstrap

First-time use needs the cache directory created. The scripts detect
its absence and exit `10`; the skill instructs the agent to ask the
user whether to:

1. **Approve** the default path,
2. **Provide an alternative** workspace root,
3. **Disable** the cache for this repo (recorded in per-developer
   auto-memory; sibling-first resolution still works).

## Scripts

### `discover`

```
discover <group>:<artifact>:<version>
discover <group>:<artifact>           # version pulled from buildSrc
discover <artifact>                   # Spine-only; group + version inferred
```

- **stdout** — absolute path to a directory you can `Grep`/`Read`.
- **stderr** — `STALE` warnings when the sibling's `versionToPublish`
  differs from the declared dependency version, plus other
  diagnostics. Always inspect.
- **exit 0** — path resolved (even if stale; the warning is on stderr).
- **exit 1** — unresolvable (missing sibling AND no sources JAR).
- **exit 10** — cache uninitialized; run the bootstrap flow.

### `extract-sources`

```
extract-sources <group>:<artifact>:<version>
```

Idempotent and race-safe. If the target directory is already populated
the script returns its path immediately. Concurrent first-time
extractions race on an atomic `mv` of a per-PID temp directory; the
loser discards its temp.

### `update-sibling`

```
update-sibling <sibling-name>            # resolved under <workspace-root>
update-sibling <absolute-path>           # acts on that path directly
```

Invoked by the agent (after user consent) when `discover` emits a
`STALE` warning. Safe by design:

- Pulls **only** when the sibling is on `master` or `main` with a
  clean working tree and a tracked upstream.
- On any other branch, exits `0` without touching anything — the
  user's "advancing multiple subprojects at once" workflow keeps
  feature branches checked out as intentional staging state.
- Refuses on detached HEAD (`3`), uncommitted changes (`4`), or
  missing upstream (`5`).
- Never switches branches, never `--rebase`, never `--force`.

On success (exit `0`), the script writes a single stable token to
**stdout** that names the outcome — callers should branch on the
token, not on stderr text. Failure paths produce empty stdout.

Exit codes:

| Code | stdout | Meaning |
|---|---|---|
| `0` | `pulled` | HEAD advanced to upstream tip |
| `0` | `up-to-date` | Already at upstream tip; nothing to do |
| `0` | `skipped-branch` | On a non-default branch; left untouched |
| `1` | _(empty)_ | Sibling not on disk |
| `2` | _(empty)_ | Not a git repository |
| `3` | _(empty)_ | Detached HEAD — refused |
| `4` | _(empty)_ | Working tree dirty — refused |
| `5` | _(empty)_ | No upstream tracking on default branch — refused |
| `6` | _(empty)_ | `git pull --ff-only` failed (divergence, network, etc.) |
| `64` | _(empty)_ | Usage error (no/too many arguments) — BSD `sysexits(3)` convention |

### `clean-cache`

```
clean-cache --dry-run
clean-cache --older-than 30d [--dry-run]
clean-cache --all [--dry-run]
```

Manual pruning only. Nothing runs on a timer.

## Conventions

- **Bash 3.2 compatible** — macOS ships 3.2 by default.
- **No external dependencies** beyond `bash`, coreutils, `grep`,
  `sed`, `awk`, `unzip`, `find`, and `git` (used only by
  `update-sibling`).
- **stdout** is always the answer; **stderr** is diagnostics. Mix
  them only by piping.
- Scripts source `lib/common.sh` after setting
  `SPINE_API_DISCOVERY_DIR`, so the workspace-root pointer file is
  reachable.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `cache not initialized` (exit 10) | Bootstrap not run | Follow the skill's bootstrap prompt |
| `sibling not on disk` | Spine repo not cloned | `git clone` it next to your consumer repo |
| `STALE: ...` | Sibling drifted from declared version | Run `update-sibling <path>` (auto-skips feature branches), or accept the warning |
| `is in the Gradle cache but publishes no -sources.jar` | Upstream artifact has no sources | Read the binary `.class` files via a different tool, or look at GitHub directly |
| `is not in the local Gradle cache` | Gradle has not fetched the dep | `./gradlew dependencies` to populate, then retry |
