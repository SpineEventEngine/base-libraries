---
name: api-discovery
description: >
  Resolve the on-disk location of a Maven artifact's source code,
  so you can inspect it directly instead of running `unzip` against JARs
  in the Gradle cache. Use this whenever you need to inspect a library's
  API or implementation — definitions of public
  types, method signatures, KDoc, internal helpers, etc.
---

# API discovery

Before reading library source code, run the `discover` script in
`.agents/scripts/api-discovery/`. It returns a path you can hand
straight to normal search and file-reading tools such as `rg`, `sed`,
or the active agent's file viewer.

Do **not** run `find ~/.gradle/caches` or `unzip` against cache JARs.
Each `unzip` decompresses the archive afresh — slow and token-heavy.

## How to call it

From the consumer repository root:

```bash
.agents/scripts/api-discovery/discover <query>
```

Where `<query>` is one of:

| Form | Example | Notes |
|---|---|---|
| `group:artifact:version` | `io.spine:spine-base:2.0.0-SNAPSHOT.390` | Most explicit |
| `group:artifact` | `io.spine:spine-base` | Version inferred from `buildSrc` |
| `artifact` | `spine-base` | Spine-only; group inferred from `buildSrc` |

The script writes the absolute resolved path to **stdout**, and any
freshness/diagnostic warnings to **stderr**. Always read stderr — a
silent stdout means clean resolution; a noisy stderr means caveats
the user should know about.

## Exit codes

| Code | Meaning | What you do |
|---|---|---|
| `0` | Path on stdout is usable. | Search or read files under that path directly. If stderr is non-empty, surface the warning to the user before relying on the path. |
| `1` | Unresolvable (no sibling AND no JAR). | Report the failure. **Do not** fall back to `unzip ~/.gradle/caches/...`. |
| `10` | Cache directory not initialized. | Run the **bootstrap flow** below. |

## Bootstrap flow (exit 10)

On the first run in a fresh workstation the per-workstation cache
directory does not yet exist. The script exits `10` and names the
path it would create. Ask the user:

> The shared cache directory `<workspace-root>/.agents/caches/api-discovery/`
> does not exist yet. How would you like to proceed?
>
> 1. **Approve** — create the directory at the default path.
> 2. **Alternative root** — pick a different parent for the shared
>    `.agents/` directory (e.g., `~/SpineWorkspace`, `/srv/spine`).
> 3. **Non-cached** — skip the extraction cache. Sibling-first
>    discovery still works for Spine artifacts; non-Spine deps will
>    not be served by `api-discovery` in this repo.

Then act on the user's reply:

- **Approve** → `mkdir -p <workspace-root>/.agents/caches/api-discovery/sources`,
  then re-run the original `discover` query.
- **Alternative root** → ask for the absolute path `<alt-root>`, then:
  ```bash
  mkdir -p "<alt-root>/.agents/caches/api-discovery/sources"
  printf '%s\n' "<alt-root>" \
      > .agents/scripts/api-discovery/.workspace-root
  ```
  (the pointer file is gitignored). Then re-run `discover`.
- **Non-cached** → record the choice in **per-developer auto-memory**
  (project memory, type `feedback`), `name: api-discovery-cache-disabled`,
  describing the user's choice and giving the "How to apply" rule:
  *do not invoke `extract-sources` in this repo; for non-Spine deps
  fall back to other investigation tools*. Then proceed with
  sibling-first only.

Check that memory at session start. If it exists, skip cache-touching
paths entirely.

## Workflow

1. **Always** call `discover` before reading library source.
2. Use the returned path with search or file-reading tools directly. Do **not**
   `cd` into the directory — that adds path-prefix noise to tool calls
   and makes line citations harder to read.
3. If stderr contains `STALE: ...`, the sibling on disk does not match
   the version declared in `buildSrc`. Surface the warning AND offer
   to refresh — see *Refreshing a stale sibling* below.
4. If the script exits `1`, report the failure with its stderr
   message and stop. Do not try `unzip` as a workaround.

## Refreshing a stale sibling

The user keeps siblings cloned locally as the source of truth and
sometimes works across several siblings at once with a feature branch
checked out in each. So a `STALE` line has two possible meanings, and
they require different handling:

- **Sibling is behind `master`/`main`.** A `git pull --ff-only` will
  bring it up to date.
- **Sibling is on a feature branch.** This is *intentional* — the user
  is staging changes across multiple subprojects. The local code is
  the right code; **do not** pull.

You cannot tell which case applies without inspecting the sibling. The
companion script `update-sibling` handles both safely: it pulls only
on the default branch with a clean tree and a tracked upstream, and
exits `0` without touching anything when on a feature branch.

### Procedure

When you see a `STALE: ...` line from `discover`:

1. Surface the warning to the user.
2. Ask, in one short prompt:
   > The sibling at `<path>` is stale. Want me to try updating it?
   > I'll only `git pull --ff-only` if it's on `master`/`main` with
   > a clean working tree; if you have a feature branch checked out,
   > I'll leave it as-is.
3. If the user agrees, run:
   ```bash
   .agents/scripts/api-discovery/update-sibling <sibling-path-or-name>
   ```
   `<sibling-path-or-name>` is either the absolute path shown by
   `discover` (preferred — unambiguous) or just the sibling repo name
   (resolved under `<workspace-root>`).
4. Read **stdout** to decide what to do next — it is a single stable
   token, not free-form English:
   - `pulled` — HEAD advanced. Re-run `discover` so the STALE warning
     clears (or, more rarely, reports a different discrepancy).
   - `up-to-date` — sibling was already at upstream tip. The STALE
     warning is informational — the declared `buildSrc` version and
     the sibling's `versionToPublish` simply disagree. Proceed
     without re-running `discover`.
   - `skipped-branch` — sibling is on a feature branch and was left
     untouched. Use the local code as-is; proceed without re-running.

   stderr always carries the human-readable diagnostics; surface it
   to the user, but do not parse it to drive control flow.
5. If the user declines, proceed without pulling. Do not ask again
   for the same sibling in the same session unless the user revisits.

### `update-sibling` exit codes

Exit 0 is split into three outcomes by the **stdout token** — read
that, not the stderr text.

| Code | stdout | Meaning | What you do |
|---|---|---|---|
| `0` | `pulled` | HEAD advanced to upstream tip. | Re-run `discover` so the STALE warning clears. |
| `0` | `up-to-date` | Already at upstream tip; nothing to do. | Proceed; surface the STALE warning to the user as informational. |
| `0` | `skipped-branch` | On a non-default branch; left untouched. | Use the local code as-is; proceed without re-running. |
| `1` | _(empty)_ | Sibling not on disk. | Report the error. |
| `2` | _(empty)_ | Not a git repository. | Report the error; do not retry. |
| `3` | _(empty)_ | Detached HEAD — refused. | Tell the user; do not retry. |
| `4` | _(empty)_ | Working tree dirty — refused. | Tell the user; do not retry. |
| `5` | _(empty)_ | No upstream tracking on default branch — refused. | Tell the user. |
| `6` | _(empty)_ | `git pull --ff-only` failed (divergence, network, etc.). | Surface the git error verbatim. |
| `64` | _(empty)_ | Usage error (no/too many arguments). | Fix the invocation; do not retry blindly. |

Failure paths produce **empty stdout** so the agent can never misread
an error message as a result token.

### "Don't ask me again"

If the user says something like "stop offering" or "skip the prompt
this session", remember that for the rest of the conversation and do
not prompt on subsequent STALE warnings — just surface the warning
and move on. This is **per-session** state; do not write it to
auto-memory.

## Anti-patterns

Stop doing these — they are exactly what this skill exists to replace:

- `find ~/.gradle/caches/modules-2/files-2.1/ -name '*-sources.jar'`
- `unzip -l <jar>` to list classes
- `unzip -p <jar> path/in/jar` to read a file
- Any chain of `unzip` + `grep` against a Gradle-cache JAR

If you find yourself wanting to do those, run `discover` instead.

## Examples

**Spine artifact, fresh sibling on disk:**

```text
$ .agents/scripts/api-discovery/discover io.spine:spine-base
/Users/<you>/Projects/Spine/base-libraries/base
$ echo $?
0
```

Follow-up searches then look like:

- `rg --files /Users/<you>/Projects/Spine/base-libraries/base`.
- `rg -n 'class Identifier' /Users/<you>/Projects/Spine/base-libraries/base`.

**Spine artifact, stale sibling:**

```text
$ .agents/scripts/api-discovery/discover io.spine.tools:validation-java
api-discovery: STALE: validation-java declared 2.0.0-SNAPSHOT.433 in Validation.kt but sibling publishes 2.0.0-SNAPSHOT.440
api-discovery: sources at /Users/<you>/Projects/Spine/validation/java may differ from the published artifact
/Users/<you>/Projects/Spine/validation/java
```

Surface the `STALE` line, then offer to refresh — see *Refreshing a
stale sibling*. After the user agrees and the pull succeeds, re-run
`discover` and the warning clears.

**Stale sibling, refresh on master:**

```text
$ .agents/scripts/api-discovery/update-sibling /Users/<you>/Projects/Spine/validation
Updating abc1234..def5678
Fast-forward
 ...
api-discovery: /Users/<you>/Projects/Spine/validation pulled 'master': abc1234... -> def5678...
pulled
$ echo $?
0
```

Stdout is `pulled` — re-run `discover` to clear the STALE warning.

**Stale sibling, already at upstream tip:**

```text
$ .agents/scripts/api-discovery/update-sibling /Users/<you>/Projects/Spine/validation
Already up to date.
api-discovery: /Users/<you>/Projects/Spine/validation already up-to-date on 'master' (def5678...)
up-to-date
$ echo $?
0
```

Stdout is `up-to-date` — the sibling is fresh; the STALE warning
reflects a declared-version vs. `versionToPublish` discrepancy that
`git pull` cannot resolve. Surface it to the user as informational.

**Stale sibling, feature branch (no-op):**

```text
$ .agents/scripts/api-discovery/update-sibling /Users/<you>/Projects/Spine/validation
api-discovery: /Users/<you>/Projects/Spine/validation is on 'feature/new-rule' (not master/main); using local code as-is
skipped-branch
$ echo $?
0
```

Stdout is `skipped-branch` — feature branch is intentional local
state. Use the code as-is.

**Non-Spine artifact, first use (extraction):**

```text
$ .agents/scripts/api-discovery/discover com.google.guava:guava:33.5.0-jre
api-discovery: extracted com.google.guava:guava:33.5.0-jre -> .../guava/33.5.0-jre
/Users/<you>/Projects/Spine/.agents/caches/api-discovery/sources/com.google.guava/guava/33.5.0-jre
```

Second call returns the same path with no stderr (already cached).

**Unresolvable:**

```text
$ .agents/scripts/api-discovery/discover io.spine:does-not-exist:9.9.9
api-discovery: io.spine:does-not-exist:9.9.9 is not in the local Gradle cache
api-discovery: run './gradlew dependencies' (or rebuild) to fetch it, then retry
$ echo $?
1
```

Report the failure verbatim; do not try `unzip` as a workaround.

## Related

- Implementation reference: `.agents/scripts/api-discovery/README.md`.
- Sibling refresh on STALE: `.agents/scripts/api-discovery/update-sibling`.
- Manual cache pruning: `.agents/scripts/api-discovery/clean-cache`.
