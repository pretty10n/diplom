"""Детальный разбор стилей шаблона ВКР."""

from __future__ import annotations

from pathlib import Path

from docx import Document
from docx.shared import Pt

path = Path("E:/Project-A/generated/vkr_template_ib.docx")
doc = Document(str(path))

print("MARGINS mm:", doc.sections[0].left_margin.mm, doc.sections[0].right_margin.mm)

for style in doc.styles:
    if not style.name.startswith("$") and style.name not in ("Heading 1", "Heading 2", "toc 1", "toc 2", "Normal"):
        continue
    if not style.name.startswith("$") and "toc" not in style.name and style.name not in ("Normal",):
        if not style.name.startswith("$"):
            continue
    try:
        f = style.font
        pf = style.paragraph_format
        size = f.size.pt if f.size else None
        print(
            f"{style.name!r}: size={size} bold={f.bold} italic={f.italic} "
            f"align={pf.alignment} indent={pf.first_line_indent} "
            f"line={pf.line_spacing} before={pf.space_before} after={pf.space_after}"
        )
    except Exception as e:
        print(style.name, e)

print("\n--- SAMPLE PARAGRAPHS BY STYLE ---")
targets = ["$_ЗАГ_СТРУКТУРНЫЙ_ЭЛЕМЕНТ", "$_ЗАГ_РАЗДЕЛ", "$_ЗАГ_ПОДРАЗДЕЛ", "$_Абзац_(обычный)", "$_Список_литературы"]
for p in doc.paragraphs:
    sn = p.style.name if p.style else ""
    if sn in targets and p.text.strip():
        r = p.runs[0] if p.runs else None
        print(f"[{sn}] {p.text[:80]}")
        if r and r.font.size:
            print(f"  run size={r.font.size.pt}")
