"""Импорт главы 1 из исходного docx ВКР в Markdown."""

from __future__ import annotations

import re
from pathlib import Path

from docx import Document
from docx.oxml.table import CT_Tbl
from docx.oxml.text.paragraph import CT_P
from docx.table import Table
from docx.text.paragraph import Paragraph

from paths import DIPLOMA_MD_DIR, GENERATED_DIR

CH1_START = "1 Теоретические основы документооборота"
CH2_START = "2 Анализ и проектирование системы автоматизации"
SECTION_RE = re.compile(r"^1\.\d+\s+")


def _iter_blocks(doc: Document):
    for child in doc.element.body.iterchildren():
        if isinstance(child, CT_P):
            yield ("p", Paragraph(child, doc))
        elif isinstance(child, CT_Tbl):
            yield ("t", Table(child, doc))


def _table_to_md(table: Table) -> str:
    rows = [[cell.text.replace("\n", " ").strip() for cell in row.cells] for row in table.rows]
    if not rows:
        return ""
    lines = ["| " + " | ".join(rows[0]) + " |", "| " + " | ".join("---" for _ in rows[0]) + " |"]
    for row in rows[1:]:
        lines.append("| " + " | ".join(row) + " |")
    return "\n".join(lines)


def extract_chapter1(source: Path) -> str:
    doc = Document(str(source))
    collecting = False
    md_lines: list[str] = ["# Глава 1. Теоретические основы документооборота в системе государственного оборонного заказа", ""]
    current_section: str | None = None

    def flush_section_header(title: str) -> None:
        nonlocal current_section
        if title.startswith("1.1 ") or title == "Нормативно-правовое регулирование государственного оборонного заказа в Российской Федерации":
            current_section = "## 1.1 Нормативно-правовое регулирование государственного оборонного заказа в Российской Федерации"
        elif SECTION_RE.match(title):
            current_section = f"## {title.strip()}"
        elif title.startswith("1 Теоретические"):
            return
        else:
            return
        if current_section and (not md_lines or md_lines[-1] != current_section):
            md_lines.extend(["", current_section, ""])

    for kind, block in _iter_blocks(doc):
        if kind == "p":
            text = block.text.strip()
            if not text:
                continue
            style = block.style.name if block.style else ""
            if text.startswith(CH2_START):
                break
            if text.startswith(CH1_START) or text == CH1_START:
                collecting = True
                continue
            if not collecting:
                continue

            if style.startswith("Heading") or text.startswith("1.3 ") or text.startswith("1.4 ") or text.startswith("1.5 "):
                flush_section_header(text)
                if SECTION_RE.match(text) or text.startswith("1.3") or text.startswith("1.4") or text.startswith("1.5"):
                    continue
                if text == "Нормативно-правовое регулирование государственного оборонного заказа в Российской Федерации":
                    continue

            if SECTION_RE.match(text):
                flush_section_header(text)
                continue

            if text.startswith("1.2 "):
                flush_section_header(text)
                md_lines.append(text)
                continue

            md_lines.append(text)
        else:
            if not collecting:
                continue
            table_md = _table_to_md(block)
            if table_md:
                md_lines.extend(["", table_md, ""])

    if current_section is None:
        md_lines.insert(2, "## 1.1 Нормативно-правовое регулирование государственного оборонного заказа в Российской Федерации")
        md_lines.insert(3, "")

    return "\n".join(md_lines).strip() + "\n"


def _ensure_source() -> Path:
    cached = GENERATED_DIR / "vkr_dushina_source.docx"
    if cached.exists():
        return cached
    import os

    desktop = Path(os.path.join(os.environ["USERPROFILE"], "Desktop"))
    for candidate in desktop.glob("*Душина*.docx"):
        if not candidate.name.startswith("~$"):
            import shutil

            shutil.copy2(candidate, cached)
            return cached
    raise FileNotFoundError("Не найден ВКР_Душина_05.06.2026.docx на рабочем столе")


def main() -> Path:
    source = _ensure_source()
    out = DIPLOMA_MD_DIR / "chapter_1.md"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(extract_chapter1(source), encoding="utf-8")
    return out


if __name__ == "__main__":
    print(main())
