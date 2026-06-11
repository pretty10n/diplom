"""Генерация списка литературы диплома ВГТУ."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from diploma_doc_builder import add_bibliography_entry, add_structural_title, save_document, setup_document
from references_data import REFERENCES
from paths import GENERATED_DIR


def build() -> Path:
    doc = setup_document()
    add_structural_title(doc, "Список литературы")
    for index, entry in enumerate(REFERENCES, start=1):
        add_bibliography_entry(doc, index, entry)
    output = GENERATED_DIR / "Список_литературы.docx"
    try:
        return save_document(doc, output)
    except PermissionError:
        return save_document(doc, GENERATED_DIR / "Список_литературы_new.docx")


if __name__ == "__main__":
    print(build())
