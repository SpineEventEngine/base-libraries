# Documentation & comments

## Commenting guidelines
- Avoid inline comments in production code unless necessary.
- Inline comments are helpful in tests.
- When using TODO comments, follow the format on the [dedicated page][todo-comments].
- File and directory names should be formatted as code.

## Protobuf file headers
- In `.proto` files, a multi-paragraph documentation header must end with a
  trailing empty comment line (`//`).
- Single-paragraph headers do not require the trailing empty comment line.

## Avoid widows, runts, orphans, or rivers

Agents should **AVOID** text flow patters illustrated
on [this diagram](widow-runt-orphan.jpg).

[todo-comments]: https://github.com/SpineEventEngine/documentation/wiki/TODO-comments
