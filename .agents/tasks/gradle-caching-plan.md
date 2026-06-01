---
slug: gradle-caching-plan
branch: gradle-review-skill
owner: claude
status: draft
started: 2026-05-29
---

# Plan: Speed Up Builds via Gradle Caching (org-wide, through `config`)

> Implementation plan for Claude Code operating in the **`SpineEventEngine/config`** repository.
> Follow the repo's existing conventions in `CLAUDE.md` / `.agents/` (commit style, copyright
> headers, Kotlin guidelines, allowed commands). Make minimal diffs and land each phase as its
> own PR.

## Purpose

Make CI and local builds across the Spine organization faster by enabling **every free Gradle
caching layer**. Because `config` is the shared submodule pulled into every Spine repository,
changes here propagate org-wide — no per-repo edits required.

## Why this work belongs in `config`

`config` is added to each Spine project as a Git submodule, and `./config/pull` copies shared
files into the consuming project. Two of those files are exactly the levers we need:

- **Root `gradle.properties`** — *overwritten* into each consuming repo on every `pull`. This is
  the single source of truth for Gradle build flags.
- **`.github-workflows/`** — its workflow scripts are *merged into* each repo's
  `.github/workflows/` on `pull`. This is where the CI definitions that run in every repo live.
  (Per the repo README, these workflows intentionally do **not** run for `config` itself, so they
  live under `.github-workflows/` rather than `.github/workflows/`.)

Editing these here, then bumping the submodule + running `./config/pull` in a consuming repo, is
how the change reaches the whole org.

## Goal

Enable, in order of safety/ROI:

1. **Dependency cache** — downloaded dependencies + wrapper distributions.
2. **Local build cache** — task outputs (`caches/build-cache-1`), persisted across CI runs so cold
   CI builds skip unchanged work.
3. **Configuration cache** — skip Gradle's configuration phase on repeat runs (gated; higher risk).

**Non-goal (out of scope here):** a *remote* build cache (Develocity or a self-hosted cache node).
That is the only layer that shares task outputs *across* repositories and across machines, but it
requires infrastructure (a reachable cache node + credentials, or Develocity) and is not a
config-only change. It is captured as a future phase, not to be implemented now.

## Mental model (so changes are made for the right reasons)

- The **dependency cache** speeds up *resolution/download*; it does not reuse build work.
- The **build cache** reuses *task outputs*, keyed by a hash of their inputs. Gradle's up-to-date
  checks already cover "same workspace, nothing changed," so the build cache only adds value from a
  **cold/fresh state** with unchanged inputs.
- **CI is cold on every run** (fresh checkout), so the build cache is precisely what helps CI —
  independent of team size or number of repos.
- `gradle/actions/setup-gradle` persists the Gradle User Home (deps, wrapper, **and**
  `caches/build-cache-1`) via the GitHub Actions cache. By default it **writes** the cache only
  from the **default branch**; other branches **read** the default branch's cache. So PR builds
  reuse what `main`'s CI produced, without polluting the shared cache.
    - Caveat: for `pull_request`-triggered runs, the cache scope is the PR merge ref and writes are
      disabled by default (only re-runs of the same PR restore them). The read-from-`main` behavior
      still applies.

## Guardrails (do / don't)

- ✅ **DO** edit the **root `gradle.properties`** in `config` for all Gradle flags.
- ⛔ **DON'T** add Gradle flags to individual consuming repos' `gradle.properties` — `./config/pull`
  overwrites that file, so such edits are lost. `config` is the only correct place.
- ✅ **DO** edit workflow templates in **`.github-workflows/`** (and, if you also want `config`'s
  own CI to benefit, `config`'s own `.github/workflows/`).
- ⛔ **DON'T** keep `actions/setup-java` with `cache: gradle` alongside `setup-gradle` — the two
  caching mechanisms conflict; remove `cache: gradle` when adding `setup-gradle`.
- ⛔ **DON'T** create any remote cache server, add secrets, create accounts, or change repo
  permissions. (Out of scope; infra/owner decisions.)
- ✅ Keep diffs minimal: don't reorder or delete existing properties/steps that are unrelated.
- ✅ Land each phase as a **separate commit/PR** and validate before moving on.

## Tasks

### Phase 0 — Inventory (no changes)

1. Read the root `gradle.properties`; record which `org.gradle.*` flags already exist (caching,
   parallel, configuration-cache, jvmargs, etc.).
2. List `.github-workflows/`. For each workflow, locate the Java/Gradle setup steps and how Gradle
   is invoked (`./gradlew ...`). Note any use of `actions/setup-java` with `cache: gradle`.
3. Check `config`'s own `.github/workflows/` separately (these run for `config` itself).
4. Read `gradle/wrapper/gradle-wrapper.properties` to determine the **Gradle version**. The stable
   configuration-cache property names below assume Gradle **8.1+**; if older, adjust property names
   and treat Phase 3 with extra caution.
5. Summarize findings before editing.

### Phase 1 — Switch CI to `gradle/actions/setup-gradle`

For each relevant workflow:

- Remove `cache: gradle` from any `actions/setup-java` step.
- Add a `gradle/actions/setup-gradle@v6` step **after** Java setup and **before** any Gradle
  invocation. (The action also configures init-scripts that apply to later `run: ./gradlew` steps.)
- Match the repo's existing action-pinning policy; current major versions available are
  `actions/checkout@v6`, `actions/setup-java@v5`, `gradle/actions/setup-gradle@v6`.

Reference shape (adapt to each workflow's actual jobs/matrix — do not blindly overwrite):

```yaml
steps:
  - uses: actions/checkout@v6
  - uses: actions/setup-java@v5
    with:
      distribution: temurin
      java-version: 17        # keep whatever the repo currently targets; no `cache: gradle`
  - uses: gradle/actions/setup-gradle@v6
  - run: ./gradlew build
```

Notes:
- The default `enhanced` cache provider is **free for public repositories** (all Spine repos are
  public). No `cache-provider` override needed unless a fully-MIT path is preferred
  (`cache-provider: basic`).
- Leave the default write-on-default-branch-only behavior in place; it's the desired setup.

### Phase 2 — Enable build cache + parallel in shared `gradle.properties`

In the root `gradle.properties`, add (only if absent):

```properties
org.gradle.caching=true
org.gradle.parallel=true
```

- `caching=true` enables the **local** build cache; combined with `setup-gradle` persisting
  `caches/build-cache-1`, CI runs now reuse task outputs.
- `parallel=true` is generally safe but must be validated (see acceptance).

### Phase 3 — Configuration cache (gated; higher risk)

In the root `gradle.properties`, add:

```properties
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn
```

- Start in **warn** mode so configuration-cache-incompatible tasks **do not fail** the build.
- Spine relies on many custom Gradle plugins and code-generation tasks (Protobuf / model compiler
  / etc.) that may not yet be configuration-cache compatible. Warn mode surfaces problems without
  breaking builds.
- Where feasible, fix incompatibilities in **`buildSrc`** (the shared build logic). If problems are
  extensive, **leave configuration cache in warn mode or defer Phase 3 entirely** — do **not**
  switch to strict/fail mode until `buildDependants` is clean.
- (On Gradle < 8.1 the stable property differs; do not guess — check the wrapper version from
  Phase 0 and use the matching property name, or skip this phase.)

### Phase 4 — Remote build cache (FUTURE — do not implement now)

Documented for completeness only. If pursued later:
- Configure `buildCache { remote(HttpBuildCache) { ... } }` (in `settings.gradle.kts` of consuming
  projects, or centrally via `buildSrc`), pushing **only from CI**.
- Per Gradle's guidance, **disable the local build cache on CI** when a remote cache is available,
  to keep GitHub Actions cache entries small.
- Requires a reachable cache node + credentials (or Develocity) and is an infrastructure decision —
  not a config-only change. Stop and flag this to a human rather than implementing it.

## Verification / acceptance criteria

`config` ships `ConfigTester`, wired into `build.gradle.kts` as the `buildDependants` task, which
checks out and builds the dependant repos (`base`, `base-types`, `core-java`) against the **local**
`config`. Use it as the gate for every phase:

```bash
./gradlew clean buildDependants   # ~30+ minutes; builds base, base-types, core-java with local config
```

Acceptance for each phase:

1. `buildDependants` **passes** with the change applied.
2. **Cache reuse is observable:** run a dependant build twice; the second run shows many tasks as
   `FROM-CACHE` / `UP-TO-DATE`.
3. **CI evidence:** in a workflow run, the `setup-gradle` **Job Summary** reports cache entries
   restored/saved; compare overall job wall-clock **before vs after**.
4. **Phase 3 specifically:** `buildDependants` completes with configuration cache enabled (warn mode
   acceptable). Record any remaining configuration-cache problems in the PR description.

## Rollout

1. Land Phases 1–2 (and 3 if clean) as separate PRs in `config`.
2. Pilot in **one** consuming repo first (suggest `base`): bump the `config` submodule, run
   `./config/pull` (this overwrites `gradle.properties` and merges `.github-workflows/` into
   `.github/workflows/`), confirm CI is green and faster.
3. Propagate to the remaining repos once the pilot is validated.

## References

- `setup-gradle` docs: https://github.com/gradle/actions/blob/main/docs/setup-gradle.md
- Gradle Build Cache: https://docs.gradle.org/current/userguide/build_cache.html
- Gradle Configuration Cache: https://docs.gradle.org/current/userguide/configuration_cache.html
- `config` README (pull mechanism, `.github-workflows`, `ConfigTester`/`buildDependants`):
  https://github.com/SpineEventEngine/config
