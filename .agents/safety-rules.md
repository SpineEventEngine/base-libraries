# Safety rules

- ✅ All code must compile and pass static analysis.
- ✅ Do not auto-update external dependencies.
- ❌ Never use reflection or unsafe code without an explicit approval.
- ❌ No analytics or telemetry code.
- ❌ No blocking calls inside coroutines.

## Commits and history-writing

**Default: do not write to git history.** This is a hard rule for every
agent — the main thread, every subagent, every skill. It overrides any
local convenience or "the change looks done" instinct.

The rule covers all of these operations:

- `git commit`, `git commit-tree`
- `git push`, `git push --force`
- `git tag`
- `git rebase`, `git merge`, `git cherry-pick` against shared history
- `git reset` that discards committed work
- `gh release create`, `gh pr merge`

Authorization to perform one of these operations exists only when **one**
of the following is true *right now*:

1. **Skill-declared.** The currently active skill's `SKILL.md` contains
   a `## Commit authorization` section that explicitly authorizes the
   operation and constrains it (which files may be staged, the exact
   commit subject, the maximum number of commits). The mere mention of
   a commit message inside skill prose is **not** authorization — the
   section heading must be present.
2. **User-instructed.** The user's *current* prompt explicitly tells
   the agent to perform the operation. Examples that qualify:
   "commit this", "make a commit with subject X", "push the branch",
   "tag this release". Authorization from previous turns, from
   `CLAUDE.md`, or from any memory file does **not** carry over.

If neither holds, the agent:

1. Stages relevant changes with `git add` (only if helpful for review).
2. Prints the proposed commit subject (if any) and `git diff --staged`.
3. **Stops.** The user runs the commit themselves, or replies with
   explicit authorization in the next prompt.

The project's `.claude/settings.json` keeps `Bash(git commit:*)` in
`permissions.ask` as defense-in-depth, but the primary enforcement is
this rule — agents must not propose commit attempts that rely on the
user clicking the prompt.
