#!/usr/bin/env python3
"""Update source copyright headers from IntelliJ IDEA copyright profiles."""

from __future__ import annotations

import argparse
import datetime as dt
import html
import re
import subprocess
import sys
from pathlib import Path
from xml.etree import ElementTree as ET


BLOCK_EXTENSIONS = {
    ".c",
    ".cc",
    ".cpp",
    ".cs",
    ".css",
    ".cxx",
    ".dart",
    ".go",
    ".gradle",
    ".groovy",
    ".h",
    ".hh",
    ".hpp",
    ".java",
    ".js",
    ".jsx",
    ".kt",
    ".kts",
    ".less",
    ".m",
    ".mm",
    ".proto",
    ".rs",
    ".scala",
    ".scss",
    ".swift",
    ".ts",
    ".tsx",
}
HASH_EXTENSIONS = {
    ".bash",
    ".bzl",
    ".properties",
    ".pl",
    ".py",
    ".rb",
    ".sh",
    ".toml",
    ".yaml",
    ".yml",
    ".zsh",
}
XML_EXTENSIONS = {
    ".fxml",
    ".pom",
    ".wsdl",
    ".xml",
    ".xsd",
    ".xsl",
    ".xslt",
}
EXCLUDED_DIRS = {
    ".agents",
    ".git",
    ".gradle",
    ".idea",
    ".kotlin",
    "build",
    "generated",
    "out",
    "tmp",
}
EXCLUDED_FILES = {
    "gradlew",
    "gradlew.bat",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Update source copyright headers from "
            ".idea/copyright/profiles_settings.xml."
        )
    )
    parser.add_argument(
        "paths",
        nargs="*",
        help="Files or directories to update. Defaults to tracked source files.",
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=Path.cwd(),
        help="Repository root. Defaults to the current working directory.",
    )
    parser.add_argument(
        "--year",
        default=str(dt.date.today().year),
        help="Year to substitute for today.year. Defaults to the current year.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Report files that would change without writing them.",
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Exit with status 1 if any file would change; do not write files.",
    )
    return parser.parse_args()


def profile_filename(profile_name: str) -> str:
    stem = re.sub(r"[^A-Za-z0-9]+", "_", profile_name).strip("_")
    if not stem:
        raise ValueError("The default copyright profile name is empty.")
    return f"{stem}.xml"


def load_notice(root: Path, year: str) -> tuple[str, Path]:
    settings_path = root / ".idea" / "copyright" / "profiles_settings.xml"
    if not settings_path.is_file():
        raise FileNotFoundError(f"Missing {settings_path}")

    settings_root = ET.parse(settings_path).getroot()
    settings = settings_root.find(".//settings")
    if settings is None:
        raise ValueError(f"{settings_path} does not contain a settings tag.")

    default_profile = settings.get("default")
    if not default_profile:
        raise ValueError(f"{settings_path} settings tag has no default attribute.")

    profile_path = settings_path.parent / profile_filename(default_profile)
    if not profile_path.is_file():
        raise FileNotFoundError(
            f"Default profile {default_profile!r} resolves to missing {profile_path}"
        )

    profile_root = ET.parse(profile_path).getroot()
    notice = None
    for option in profile_root.findall(".//option"):
        if option.get("name") == "notice":
            notice = option.get("value")
            break
    if notice is None:
        raise ValueError(f"{profile_path} has no option named 'notice'.")

    decoded = html.unescape(notice)
    decoded = decoded.replace("${today.year}", year)
    decoded = decoded.replace("$today.year", year)
    decoded = decoded.replace("today.year", year)
    return decoded.rstrip(), profile_path


def style_for(path: Path) -> str | None:
    name = path.name
    suffix = path.suffix.lower()
    if name.endswith((".sh.template", ".bash.template", ".zsh.template")):
        return "hash"
    if suffix in BLOCK_EXTENSIONS:
        return "block"
    if suffix in HASH_EXTENSIONS:
        return "hash"
    if suffix in XML_EXTENSIONS:
        return "xml"
    return None


def is_excluded(path: Path) -> bool:
    if path.name in EXCLUDED_FILES:
        return True
    parts = path.parts
    if len(parts) >= 2 and parts[0] == "gradle" and parts[1] == "wrapper":
        return True
    return any(part in EXCLUDED_DIRS for part in parts)


def tracked_files(root: Path) -> list[Path]:
    try:
        result = subprocess.run(
            ["git", "-C", str(root), "ls-files", "-z"],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
    except (FileNotFoundError, subprocess.CalledProcessError):
        return [
            path.relative_to(root)
            for path in root.rglob("*")
            if path.is_file() and not is_excluded(path.relative_to(root))
        ]

    paths = []
    for item in result.stdout.decode("utf-8").split("\0"):
        if not item:
            continue
        path = Path(item)
        if (root / path).is_file():
            paths.append(path)
    return paths


def expand_requested_paths(root: Path, requested: list[str]) -> list[Path]:
    if not requested:
        paths = tracked_files(root)
    else:
        paths = []
        for item in requested:
            path = (root / item).resolve()
            if not path.exists():
                raise FileNotFoundError(f"Path does not exist: {item}")
            if not path.is_relative_to(root):
                raise ValueError(
                    f"Path is outside the repository root: {item!r} "
                    f"(resolved to {path}, root is {root})"
                )
            if path.is_dir():
                for child in path.rglob("*"):
                    if child.is_file():
                        paths.append(child.relative_to(root))
            else:
                paths.append(path.relative_to(root))

    unique = sorted(set(paths), key=lambda p: p.as_posix())
    return [
        path
        for path in unique
        if style_for(path) is not None and not is_excluded(path)
    ]


def newline_for(text: str) -> str:
    return "\r\n" if "\r\n" in text else "\n"


def build_header(notice: str, style: str, newline: str) -> str:
    lines = notice.splitlines()
    if style == "block":
        body = newline.join(f" * {line}" if line else " *" for line in lines)
        return f"/*{newline}{body}{newline} */{newline}{newline}"
    if style == "hash":
        body = newline.join(f"# {line}" if line else "#" for line in lines)
        return f"{body}{newline}{newline}"
    if style == "xml":
        body = newline.join(f"  ~ {line}" if line else "  ~" for line in lines)
        return f"<!--{newline}{body}{newline}  -->{newline}{newline}"
    raise ValueError(f"Unsupported comment style: {style}")


def split_leading_directive(text: str, style: str, newline: str) -> tuple[str, str]:
    if style == "hash" and text.startswith("#!"):
        line_end = text.find("\n")
        if line_end == -1:
            return text + newline + newline, ""
        prefix = text[: line_end + 1] + newline
        return prefix, strip_leading_blank_lines(text[line_end + 1 :])

    if style == "xml" and text.startswith("<?xml"):
        close = text.find("?>")
        if close != -1:
            line_end = text.find("\n", close)
            if line_end == -1:
                return text + newline + newline, ""
            prefix = text[: line_end + 1] + newline
            return prefix, strip_leading_blank_lines(text[line_end + 1 :])

    return "", strip_leading_blank_lines(text)


def strip_leading_blank_lines(text: str) -> str:
    return re.sub(r"^(?:[ \t]*\r?\n)+", "", text)


def strip_existing_header(text: str, style: str) -> tuple[str, bool]:
    if style == "block" and text.startswith("/*"):
        close = text.find("*/")
        if close != -1:
            candidate = text[: close + 2]
            if is_copyright_header(candidate):
                return strip_leading_blank_lines(text[close + 2 :]), True

    if style == "xml" and text.startswith("<!--"):
        close = text.find("-->")
        if close != -1:
            candidate = text[: close + 3]
            if is_copyright_header(candidate):
                return strip_leading_blank_lines(text[close + 3 :]), True

    if style == "hash":
        lines = text.splitlines(keepends=True)
        end = 0
        for line in lines:
            stripped = line.strip()
            if stripped == "" or stripped.startswith("#"):
                end += len(line)
                continue
            break
        candidate = text[:end]
        if candidate and is_copyright_header(candidate):
            return strip_leading_blank_lines(text[end:]), True

    return text, False


def is_copyright_header(text: str) -> bool:
    limited = text[:5000]
    return "Copyright" in limited and (
        "Licensed under" in limited or "All rights reserved" in limited
    )


def updated_text(text: str, notice: str, style: str) -> str:
    original = text
    bom = "\ufeff" if text.startswith("\ufeff") else ""
    if bom:
        text = text[1:]
    newline = newline_for(text)
    prefix, body = split_leading_directive(text, style, newline)
    body, had_header = strip_existing_header(body, style)
    if not had_header:
        return original
    return bom + prefix + build_header(notice, style, newline) + body


def update_file(root: Path, path: Path, notice: str, dry_run: bool) -> bool:
    absolute = root / path
    style = style_for(path)
    if style is None:
        return False

    try:
        text = absolute.read_text(encoding="utf-8")
    except FileNotFoundError:
        print(f"Skipping missing file: {path}", file=sys.stderr)
        return False
    except UnicodeDecodeError:
        print(f"Skipping non-UTF-8 file: {path}", file=sys.stderr)
        return False

    next_text = updated_text(text, notice, style)
    if next_text == text:
        return False

    if not dry_run:
        with absolute.open("w", encoding="utf-8", newline="") as file:
            file.write(next_text)
    return True


def main() -> int:
    args = parse_args()
    root = args.root.resolve()
    notice, profile_path = load_notice(root, args.year)
    try:
        paths = expand_requested_paths(root, args.paths)
    except (FileNotFoundError, ValueError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 2
    dry_run = args.dry_run or args.check

    changed = [
        path
        for path in paths
        if update_file(root, path, notice, dry_run=dry_run)
    ]

    rel_profile = profile_path.relative_to(root)
    action = "Would update" if dry_run else "Updated"
    print(f"Notice source: {rel_profile}")
    print(f"{action} {len(changed)} file(s).")
    for path in changed:
        print(path.as_posix())

    if args.check and changed:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
