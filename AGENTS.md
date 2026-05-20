# 👋 Welcome, Agents!

For detailed agent guidelines and documentation, please see:

**[Agent Documentation](./.agents/_TOC.md)**

## Moving Files

When moving or renaming tracked files, always use `git mv`.

Do not simulate a move by deleting the old file and creating a new file. Preserve
Git history unless the user explicitly asks for a fresh file replacement.

If `git mv` fails because of permissions or sandbox restrictions, request
approval to run `git mv`; do not fall back to delete/create.
