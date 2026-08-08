#!/usr/bin/env python3

from __future__ import annotations

import datetime as dt
import sys
from pathlib import Path
from string import Template


SET_TYPES = (
    "EXPANSION",
    "CORE",
    "MAGIC_ONLINE",
    "MAGIC_ARENA",
    "SUPPLEMENTAL",
    "SUPPLEMENTAL_STANDARD_LEGAL",
    "SUPPLEMENTAL_MODERN_LEGAL",
    "PROMOTIONAL",
    "REMIX",
    "JOKE_SET",
    "CUSTOM_SET",
)


def prompt_non_empty(label: str) -> str:
    while True:
        value = input(f"{label}: ").strip()
        if value:
            return value
        print(f"{label} is required.")


def prompt_release_date() -> dt.date:
    while True:
        value = input("Set release date (YYYY-MM-DD): ").strip()
        try:
            return dt.datetime.strptime(value, "%Y-%m-%d").date()
        except ValueError:
            print("Release date must be in YYYY-MM-DD format.")


def prompt_set_type() -> str:
    print("Valid set types:")
    for set_type in SET_TYPES:
        print(f"  {set_type}")

    while True:
        value = input("Set type: ").strip().upper()
        if value in SET_TYPES:
            return value
        print("Set type must match one of the values above.")


def to_set_class_name(name: str) -> str:
    normalized = name.replace("'", "").replace("&", " And ")
    words = []
    current = []

    for char in normalized:
        if char.isascii() and char.isalnum():
            current.append(char)
            continue

        if current:
            words.append("".join(current))
            current = []

    if current:
        words.append("".join(current))

    return "".join(word[:1].upper() + word[1:] for word in words)


def load_author(utils_dir: Path) -> str:
    author_path = utils_dir / "data" / "author.txt"
    if not author_path.is_file():
        return "anonymous"

    author = author_path.read_text(encoding="utf-8").splitlines()
    if not author:
        return "anonymous"

    return author[0].strip() or "anonymous"


def load_template(utils_dir: Path) -> Template:
    template_path = utils_dir / "setClass.tmpl"
    return Template(template_path.read_text(encoding="utf-8"))


def load_set_rows(sets_path: Path) -> list[tuple[str, str]]:
    rows: list[tuple[str, str]] = []
    for raw_line in sets_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line:
            continue

        parts = line.split("|")
        if len(parts) < 2:
            raise ValueError(f"Invalid set row: {raw_line}")

        name = parts[0].strip()
        code = parts[1].strip()
        rows.append((name, code))

    return rows


def insert_set_row(rows: list[tuple[str, str]], set_name: str, set_code: str) -> list[tuple[str, str]]:
    for existing_name, _ in rows:
        if existing_name == set_name:
            raise ValueError(f"Set name already exists in mtg-sets-data.txt: {set_name}")

    for _, existing_code in rows:
        if existing_code == set_code:
            raise ValueError(f"Set code already exists in mtg-sets-data.txt: {set_code}")

    new_row = (set_name, set_code)
    insert_at = len(rows)

    for index, (existing_name, _) in enumerate(rows):
        if set_name.casefold() < existing_name.casefold():
            insert_at = index
            break

    updated = rows[:insert_at] + [new_row] + rows[insert_at:]
    return updated


def write_set_rows(sets_path: Path, rows: list[tuple[str, str]]) -> None:
    output = "\n".join(f"{name}|{code}|" for name, code in rows) + "\n"
    sets_path.write_text(output, encoding="utf-8", newline="\n")


def main() -> int:
    utils_dir = Path(__file__).resolve().parent
    repo_root = utils_dir.parent
    sets_dir = repo_root / "Mage.Sets" / "src" / "mage" / "sets"
    sets_data_path = utils_dir / "mtg-sets-data.txt"

    set_name = prompt_non_empty("Set Name")
    set_code = prompt_non_empty("Set Code")
    release_date = prompt_release_date()
    set_type = prompt_set_type()

    class_name = to_set_class_name(set_name)
    output_path = sets_dir / f"{class_name}.java"

    if output_path.exists():
        print(f"Refusing to overwrite existing file: {output_path}", file=sys.stderr)
        return 1

    try:
        rows = load_set_rows(sets_data_path)
        updated_rows = insert_set_row(rows, set_name, set_code)
    except ValueError as exc:
        print(str(exc), file=sys.stderr)
        return 1

    author = load_author(utils_dir)
    template = load_template(utils_dir)
    output = template.substitute(
        AUTHOR=author,
        CLASSNAME=class_name,
        SETNAME=set_name,
        SETCODE=set_code,
        YEAR=release_date.year,
        MONTH=release_date.month,
        DAY=release_date.day,
        SETTYPE=set_type,
    )

    output_path.write_text(output, encoding="utf-8", newline="\n")
    write_set_rows(sets_data_path, updated_rows)

    print(f"Created {output_path}")
    print(f"Updated {sets_data_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
