"""Загрузка разделов из Markdown-черновиков диплома."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path


@dataclass
class Section:
    heading: str
    paragraphs: list[str]


def _parse_sections(text: str) -> list[Section]:
    lines = text.splitlines()
    sections: list[Section] = []
    current_heading = ""
    current_paragraphs: list[str] = []
    buffer: list[str] = []

    def flush_paragraph() -> None:
        nonlocal buffer
        if buffer:
            paragraph = " ".join(line.strip() for line in buffer if line.strip())
            if paragraph:
                current_paragraphs.append(paragraph)
            buffer = []

    for line in lines:
        if line.startswith("# ") and not line.startswith("## "):
            continue
        if line.startswith("## "):
            flush_paragraph()
            if current_heading:
                sections.append(Section(current_heading, current_paragraphs))
            current_heading = line[3:].strip()
            current_paragraphs = []
            continue
        if not line.strip():
            flush_paragraph()
            continue
        buffer.append(line)
    flush_paragraph()
    if current_heading:
        sections.append(Section(current_heading, current_paragraphs))
    return sections


def load_chapter_sections(md_path: Path, *extra_paths: Path) -> list[Section]:
    sections = _parse_sections(md_path.read_text(encoding="utf-8"))
    index = {s.heading: s for s in sections}
    for extra_path in extra_paths:
        if not extra_path.exists():
            continue
        for extra in _parse_sections(extra_path.read_text(encoding="utf-8")):
            if extra.heading in index:
                index[extra.heading].paragraphs.extend(extra.paragraphs)
            else:
                sections.append(extra)
                index[extra.heading] = extra
    return sections
