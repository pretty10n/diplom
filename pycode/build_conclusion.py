"""Генерация заключения диплома ВГТУ."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from diploma_doc_builder import add_paragraph, add_structural_title, save_document, setup_document
from paths import DIPLOMA_MD_DIR, GENERATED_DIR


def _load_conclusion_paragraphs(md_path: Path) -> list[str]:
    paragraphs: list[str] = []
    buffer: list[str] = []
    for line in md_path.read_text(encoding="utf-8").splitlines():
        if line.startswith("#"):
            continue
        if not line.strip():
            if buffer:
                paragraphs.append(" ".join(buffer))
                buffer = []
            continue
        buffer.append(line.strip())
    if buffer:
        paragraphs.append(" ".join(buffer))
    return paragraphs


def build() -> Path:
    doc = setup_document()
    add_structural_title(doc, "Заключение")
    for paragraph in _load_conclusion_paragraphs(DIPLOMA_MD_DIR / "conclusion.md"):
        add_paragraph(doc, paragraph)
    output = GENERATED_DIR / "Заключение.docx"
    try:
        return save_document(doc, output)
    except PermissionError:
        return save_document(doc, GENERATED_DIR / "Заключение_new.docx")


if __name__ == "__main__":
    print(build())
