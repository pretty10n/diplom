"""Генерация Главы 1 диплома ВГТУ (из chapter_1.md)."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from chapter1_tables import TABLE_1_HEADERS, TABLE_1_ROWS
from diploma_doc_builder import (
    add_chapter_title,
    add_paragraph,
    add_section_heading,
    add_table,
    save_document,
    setup_document,
)
from diploma_md_loader import load_chapter_sections
from import_chapter1_from_vkr import main as import_chapter1
from paths import DIPLOMA_MD_DIR, GENERATED_DIR


def _is_md_table_line(text: str) -> bool:
    t = text.strip()
    return t.startswith("|") or t.startswith("Таблица 1 -") or t == "---"


def build() -> Path:
    if not (DIPLOMA_MD_DIR / "chapter_1.md").exists():
        import_chapter1()

    doc = setup_document()
    add_chapter_title(doc, "1 Теоретические основы документооборота в системе государственного оборонного заказа")

    sections = load_chapter_sections(DIPLOMA_MD_DIR / "chapter_1.md")
    for section in sections:
        add_section_heading(doc, section.heading)
        table_added = False
        for paragraph in section.paragraphs:
            if _is_md_table_line(paragraph):
                continue
            add_paragraph(doc, paragraph)
            if (
                not table_added
                and section.heading.startswith("1.3")
                and "таблицу 1" in paragraph.lower()
            ):
                add_table(doc, TABLE_1_HEADERS, TABLE_1_ROWS)
                table_added = True

    output = GENERATED_DIR / "Глава_1_Анализ.docx"
    try:
        return save_document(doc, output)
    except PermissionError:
        return save_document(doc, GENERATED_DIR / "Глава_1_Анализ_new.docx")


if __name__ == "__main__":
    print(build())
