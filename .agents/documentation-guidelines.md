# Documentation & comments

## Commenting guidelines
- Avoid inline comments in production code unless necessary.
- Inline comments are helpful in tests.
- When using TODO comments, follow the format on the [dedicated page][todo-comments].
- File and directory names should be formatted as code.

## API documentation scope

KDoc and Javadoc describe the API as it appears to a consumer of the published
artifact. Keep them focused on behaviour, parameters, return values, and usage
examples.

Do **not** reference repository-internal locations from API docs:

- Build infrastructure paths such as `buildSrc/` or `config/` (the `config`
  repository, `config/buildSrc/`, and similar).
- Agent-facing material under `.agents/` — task plans, skill rules, review
  notes, conventions, or any other file rooted there.
- Branch names, commit SHAs, issue numbers, or other repo workflow artefacts.

These details are invisible to a consumer who only sees the artifact's
sources/Javadoc/KDoc and rot quickly as the repository evolves. If the rationale
for an API decision lives in such a file, summarise the *outcome* in the
KDoc instead of linking to the source. Cross-repository parity notes and
work-in-progress justifications belong in the task plan under
`.agents/tasks/`, not in the published API documentation.

## Protobuf file headers
- In `.proto` files, a multi-paragraph documentation header must end with a
  trailing empty comment line (`//`).
- Single-paragraph headers do not require the trailing empty comment line.

## Avoid widows, runts, orphans, or rivers

Agents should **AVOID** text flow patters illustrated
on [this diagram](widow-runt-orphan.jpg).

[todo-comments]: https://github.com/SpineEventEngine/documentation/wiki/TODO-comments
