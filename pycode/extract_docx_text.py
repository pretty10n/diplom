"""Извлечение текста и таблиц из docx для анализа глав."""

from __future__ import annotations

import sys
from pathlib import Path

from docx import Document


def dump_docx(path: Path, out_path: Path) -> None:
    doc = Document(str(path))
    lines: list[str] = []
    for block in _iter_block_items(doc):
        if block["kind"] == "p":
            text = block["text"].strip()
            if text:
                style = block.get("style", "")
                lines.append(f"[P:{style}] {text}")
        elif block["kind"] == "t":
            lines.append("[TABLE]")
            for row in block["rows"]:
                lines.append(" | ".join(cell.strip() for cell in row))
            lines.append("[/TABLE]")
    out_path.write_text("\n".join(lines), encoding="utf-8")


def _iter_block_items(doc):
    from docx.oxml.text.paragraph import CT_P
    from docx.oxml.table import CT_Tbl
    from docx.table import Table
    from docx.text.paragraph import Paragraph

    for child in doc.element.body.iterchildren():
        if isinstance(child, CT_P):
            p = Paragraph(child, doc)
            yield {"kind": "p", "text": p.text, "style": p.style.name if p.style else ""}
        elif isinstance(child, CT_Tbl):
            table = Table(child, doc)
            rows = []
            for row in table.rows:
                rows.append([cell.text.replace("\n", " ") for cell in row.cells])
            yield {"kind": "t", "rows": rows}


if __name__ == "__main__":
    src = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("E:/Project-A/generated/vkr_dushina_source.docx")
    out = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("E:/Project-A/pycode/_vkr_extract.txt")
    dump_docx(src, out)
    print(out)
