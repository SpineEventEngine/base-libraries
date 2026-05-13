from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "scripts" / "update_copyright.py"


class UpdateCopyrightTest(unittest.TestCase):
    def test_default_run_leaves_plain_source_without_header_unchanged(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_profile(root)
            source = root / "Foo.java"
            original = "class Foo {}\n"
            source.write_text(original, encoding="utf-8")

            subprocess.run(["git", "init", "-q"], cwd=root, check=True)
            subprocess.run(["git", "add", "Foo.java"], cwd=root, check=True)

            result = self.run_script(root)

            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertIn("Updated 0 file(s).", result.stdout)
            self.assertEqual(result.stderr, "")
            self.assertEqual(source.read_text(encoding="utf-8"), original)

    def test_existing_header_is_updated(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_profile(root)
            source = root / "Foo.java"
            source.write_text(
                "/*\n"
                " * Copyright 2024 ACME\n"
                " * All rights reserved\n"
                " */\n"
                "\n"
                "class Foo {}\n",
                encoding="utf-8",
            )

            result = self.run_script(root, "--year", "2026", "Foo.java")

            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertIn("Updated 1 file(s).", result.stdout)
            self.assertIn("Foo.java", result.stdout)
            self.assertEqual(result.stderr, "")
            self.assertEqual(
                source.read_text(encoding="utf-8"),
                "/*\n"
                " * Copyright 2026 ACME\n"
                " * All rights reserved\n"
                " */\n"
                "\n"
                "class Foo {}\n",
            )

    def test_default_run_skips_tracked_files_deleted_from_working_tree(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_profile(root)
            source = root / "Foo.java"
            source.write_text("class Foo {}\n", encoding="utf-8")

            subprocess.run(["git", "init", "-q"], cwd=root, check=True)
            subprocess.run(["git", "add", "Foo.java"], cwd=root, check=True)
            source.unlink()

            result = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    "--root",
                    str(root),
                    "--dry-run",
                ],
                check=False,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
            )

            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertIn("Would update 0 file(s).", result.stdout)
            self.assertEqual(result.stderr, "")

    @staticmethod
    def run_script(root: Path, *args: str) -> subprocess.CompletedProcess[str]:
        return subprocess.run(
            [
                sys.executable,
                str(SCRIPT),
                "--root",
                str(root),
                *args,
            ],
            check=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )

    @staticmethod
    def write_profile(root: Path) -> None:
        copyright_dir = root / ".idea" / "copyright"
        copyright_dir.mkdir(parents=True)
        (copyright_dir / "profiles_settings.xml").write_text(
            '<component name="CopyrightManager">'
            '<settings default="Default" />'
            "</component>\n",
            encoding="utf-8",
        )
        (copyright_dir / "Default.xml").write_text(
            '<component name="CopyrightManager">'
            "<copyright>"
            '<option name="notice" '
            'value="Copyright ${today.year} ACME&#10;All rights reserved" />'
            "</copyright>"
            "</component>\n",
            encoding="utf-8",
        )


if __name__ == "__main__":
    unittest.main()
