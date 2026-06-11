"""Генерация введения ВКР."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from build_conclusion import _load_conclusion_paragraphs
from diploma_doc_builder import add_paragraph, add_structural_title, save_document, setup_document
from paths import DIPLOMA_MD_DIR, GENERATED_DIR


def build() -> Path:
    doc = setup_document()
    add_structural_title(doc, "Введение")
    for paragraph in _load_conclusion_paragraphs(DIPLOMA_MD_DIR / "introduction.md"):
        add_paragraph(doc, paragraph)
    output = GENERATED_DIR / "Введение.docx"
    try:
        return save_document(doc, output)
    except PermissionError:
        return save_document(doc, GENERATED_DIR / "Введение_new.docx")


if __name__ == "__main__":
    print(build())
