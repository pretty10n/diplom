"""Генерация иллюстраций для глав 2–3 диплома."""

from __future__ import annotations

import matplotlib.pyplot as plt
from matplotlib.patches import Circle, Ellipse, FancyArrowPatch, FancyBboxPatch, Polygon

from paths import FIGURES_DIR

plt.rcParams["font.family"] = "DejaVu Sans"
plt.rcParams["axes.unicode_minus"] = False


def _save(fig, name: str) -> str:
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    path = FIGURES_DIR / name
    fig.savefig(path, dpi=150, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    return str(path)


def _activity_arrow(ax, x1: float, y1: float, x2: float, y2: float, *, text: str = "") -> None:
    ax.annotate(
        "",
        xy=(x2, y2),
        xytext=(x1, y1),
        arrowprops=dict(arrowstyle="-|>", lw=1.1, color="#333", shrinkA=2, shrinkB=2),
    )
    if text:
        ax.text((x1 + x2) / 2 + 0.08, (y1 + y2) / 2, text, fontsize=7, color="#555")


def _activity_node(ax, cx: float, cy: float, w: float, h: float, text: str) -> tuple[float, float, float, float]:
    x, y = cx - w / 2, cy - h / 2
    box = FancyBboxPatch(
        (x, y), w, h, boxstyle="round,pad=0.04",
        linewidth=1.1, edgecolor="#333", facecolor="#E8F4FD",
    )
    ax.add_patch(box)
    ax.text(cx, cy, text, ha="center", va="center", fontsize=7.5)
    return x, y, w, h


def _decision_node(ax, cx: float, cy: float, size: float, text: str) -> float:
    half = size / 2
    diamond = Polygon(
        [(cx, cy + half), (cx + half, cy), (cx, cy - half), (cx - half, cy)],
        closed=True, linewidth=1.1, edgecolor="#333", facecolor="#FFF9E6",
    )
    ax.add_patch(diamond)
    ax.text(cx, cy, text, ha="center", va="center", fontsize=6.8)
    return half


def _terminal_node(ax, cx: float, cy: float, *, end: bool = False) -> None:
    r = 0.14
    if end:
        ax.add_patch(Circle((cx, cy), r + 0.05, fill=False, linewidth=1.2, edgecolor="#333"))
    ax.add_patch(Circle((cx, cy), r, linewidth=1.2, edgecolor="#333", facecolor="#333" if not end else "white"))


def draw_activity_bp01() -> str:
    """Диаграмма деятельности (блок-схема алгоритма) BP-01."""
    fig, ax = plt.subplots(figsize=(8.5, 14))
    ax.set_xlim(0, 8.5)
    ax.set_ylim(0, 14)
    ax.axis("off")

    cx = 4.25
    y = 13.35
    step = 0.72

    _terminal_node(ax, cx, y)
    y -= 0.35
    _activity_arrow(ax, cx, y + 0.35, cx, y + 0.28)

    nodes: list[tuple[float, str, float, float]] = [
        (y, "Открыть мастер ввода", 2.8, 0.52),
        (y - step, "Шаг 1: заполнить общий блок\n(ТРУ, этап, годы)", 3.4, 0.62),
        (y - 2 * step, "Сохранить черновик\n(POST/PATCH /documents)", 3.5, 0.62),
    ]
    for cy, label, w, h in nodes:
        _activity_node(ax, cx, cy, w, h, label)

    y_loop_top = y - 2 * step - 0.45
    _activity_arrow(ax, cx, y - 2 * step - 0.31, cx, y_loop_top + 0.26)

    loop_y = y_loop_top
    loop_nodes = [
        (loop_y, "Выбрать раздел из справочника", 3.5, 0.52),
        (loop_y - step, "Определить formNo\nпо sectionKey (4/5/6)", 3.2, 0.62),
        (loop_y - 2 * step, "Заполнить поля строки\n(col2..col15)", 3.2, 0.62),
    ]
    for cy, label, w, h in loop_nodes:
        _activity_node(ax, cx, cy, w, h, label)

    d1_y = loop_y - 2 * step - 0.55
    _activity_arrow(ax, cx, loop_y - 2 * step - 0.31, cx, d1_y + 0.28)
    d_half = _decision_node(ax, cx, d1_y, 1.05, "Данные\nвалидны?")

    err_y = d1_y
    err_x = 1.45
    _activity_node(ax, err_x, err_y, 2.0, 0.58, "Показать\nошибку")
    _activity_arrow(ax, cx - d_half, d1_y, err_x + 1.0, d1_y, text="нет")
    fill_y = loop_y - 2 * step
    ax.plot([err_x, err_x], [err_y - 0.29, fill_y], color="#333", lw=1.0)
    _activity_arrow(ax, err_x, fill_y, cx - 1.6, fill_y)

    ok_y = d1_y - step
    _activity_node(ax, cx, ok_y, 3.0, 0.62, "Рассчитать col11/col12\nи сохранить строку")
    _activity_arrow(ax, cx, d1_y - d_half, cx, ok_y + 0.31, text="да")

    d2_y = ok_y - 0.72
    _activity_arrow(ax, cx, ok_y - 0.31, cx, d2_y + 0.28)
    d2_half = _decision_node(ax, cx, d2_y, 1.0, "Ещё\nстроки?")

    merge_y = d2_y - 0.85
    _activity_arrow(ax, cx, d2_y - d2_half, cx, merge_y + 0.31, text="нет")
    _activity_node(ax, cx, merge_y, 3.0, 0.52, "Шаг 3: предпросмотр\n(GET /entries, /totals)")

    d3_y = merge_y - 0.78
    _activity_arrow(ax, cx, merge_y - 0.26, cx, d3_y + 0.28)
    d3_half = _decision_node(ax, cx, d3_y, 1.05, "Нет invalid\nи агрегаты\nкорректны?")

    back_x = 7.35
    ax.plot([back_x, back_x], [loop_y + 0.26, d3_y], color="#333", lw=1.0)
    ax.plot([cx + d3_half, back_x], [d3_y, d3_y], color="#333", lw=1.0)
    _activity_arrow(ax, back_x, d3_y, back_x, loop_y + 0.26, text="нет")
    _activity_arrow(ax, back_x, loop_y + 0.26, cx + 1.75, loop_y + 0.26)
    ax.text(7.55, (loop_y + d3_y) / 2, "исправить\nстроки", fontsize=6.5, ha="left", va="center", color="#555")

    export_y = d3_y - 0.85
    _activity_arrow(ax, cx, d3_y - d3_half, cx, export_y + 0.31, text="да")
    _activity_node(ax, cx, export_y, 3.3, 0.62, "Экспорт XLSX\n(POST /export, fill_template)")

    d4_y = export_y - 0.78
    _activity_arrow(ax, cx, export_y - 0.31, cx, d4_y + 0.28)
    d4_half = _decision_node(ax, cx, d4_y, 1.0, "entries\n> 0?")

    end_y = d4_y - 0.72
    _activity_arrow(ax, cx, d4_y - d4_half, cx, end_y + 0.22, text="да")
    _activity_node(ax, cx, end_y + 0.18, 2.4, 0.48, "Скачать .xlsx")

    stop_y = end_y - 0.35
    _activity_arrow(ax, cx, end_y - 0.06, cx, stop_y + 0.2)
    _terminal_node(ax, cx, stop_y, end=True)

    yes_loop_x = 0.85
    ax.plot([yes_loop_x, yes_loop_x], [d2_y - d2_half, loop_y + 0.26], color="#333", lw=1.0)
    _activity_arrow(ax, yes_loop_x, d2_y - d2_half, yes_loop_x, loop_y + 0.26)
    ax.text(yes_loop_x - 0.08, (d2_y + loop_y) / 2, "да", fontsize=7, ha="right", va="center", color="#555")
    _activity_arrow(ax, yes_loop_x, loop_y + 0.26, cx - 1.75, loop_y + 0.26)

    ax.text(
        4.25, 13.75,
        "BP-01: диаграмма деятельности заполнения форм 4/5/6",
        ha="center", fontsize=10.5, weight="bold",
    )
    return _save(fig, "fig_2_2_activity_bp01.png")


def _arch_layer(ax, y: float, h: float, label: str) -> None:
    ax.add_patch(
        FancyBboxPatch(
            (1.55, y), 8.3, h, boxstyle="square,pad=0",
            linewidth=1.2, edgecolor="#333", facecolor="#FAD7A0",
        )
    )
    ax.text(0.2, y + h / 2, label, ha="left", va="center", fontsize=8.5, weight="bold")


def _arch_component(ax, cx: float, cy: float, w: float, h: float, text: str) -> tuple[float, float]:
    x, y = cx - w / 2, cy - h / 2
    ax.add_patch(
        FancyBboxPatch(
            (x, y), w, h, boxstyle="round,pad=0.04",
            linewidth=1.1, edgecolor="#333", facecolor="#AED6F1",
        )
    )
    ax.text(cx, cy, text, ha="center", va="center", fontsize=8)
    return cx, cy


def _arch_arrow(
    ax, x1: float, y1: float, x2: float, y2: float, *, color: str, rad: float = 0.0,
) -> None:
    style = "Simple,tail_width=0.4,head_width=6,head_length=6"
    ax.add_patch(
        FancyArrowPatch(
            (x1, y1), (x2, y2),
            arrowstyle=style, mutation_scale=0.9, lw=1.1,
            linestyle="dashed", color=color, connectionstyle=f"arc3,rad={rad}",
        )
    )


def draw_architecture() -> str:
    """Многоуровневая архитектура в стиле шаблона: 4 слоя, пунктирные стрелки запрос/ответ."""
    fig, ax = plt.subplots(figsize=(10, 7.5))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 7.5)
    ax.axis("off")

    layer_h = 1.35
    gap = 0.18
    y_db = 0.35
    y_dao = y_db + 1.05 + gap
    y_svc = y_dao + layer_h + gap
    y_pres = y_svc + layer_h + gap

    _arch_layer(ax, y_pres, layer_h, "Уровень\nпредставления")
    _arch_layer(ax, y_svc, layer_h, "Бизнес-\nлогика")
    _arch_layer(ax, y_dao, layer_h, "Уровень\nхранения\nданных")
    _arch_layer(ax, y_db, 1.05, "Уровень абстракции\nбаз данных")

    cy_pres = y_pres + layer_h / 2
    cy_svc = y_svc + layer_h / 2
    cy_dao = y_dao + layer_h / 2
    cy_db = y_db + 0.52

    ui = _arch_component(ax, 3.5, cy_pres, 2.5, 0.72, "Экран\nпользователя\n(React SPA)")
    client = _arch_component(ax, 7.2, cy_pres, 2.5, 0.72, "API Client\n(api/client.ts)")
    service = _arch_component(ax, 7.2, cy_svc, 3.0, 0.78, "Сервисный слой\n(DocumentApiService,\nReferenceDataService)")
    doc_dao = _arch_component(ax, 3.5, cy_dao, 2.5, 0.72, "DAO документа\n(Spring Data JPA)")
    ref_dao = _arch_component(ax, 7.2, cy_dao, 2.5, 0.72, "DAO справочника\n(Spring Data JPA)")

    db_w, db_h = 3.0, 0.95
    ax.add_patch(
        Ellipse(
            (5.4, cy_db), db_w, db_h,
            linewidth=1.2, edgecolor="#1B4F72", facecolor="#2874A6",
        )
    )
    ax.text(5.4, cy_db, "База данных\n(PostgreSQL 15)", ha="center", va="center",
            fontsize=8.5, color="white", weight="bold")

    user_x = 1.05
    _arch_arrow(ax, user_x, cy_pres + 0.55, ui[0] - 1.25, cy_pres + 0.1, color="#111")
    _arch_arrow(ax, ui[0] - 1.25, cy_pres - 0.1, user_x, cy_pres - 0.55, color="#C0392B")

    _arch_arrow(ax, ui[0] + 1.25, cy_pres, client[0] - 1.25, cy_pres, color="#111")
    _arch_arrow(ax, client[0] - 1.25, cy_pres - 0.08, ui[0] + 1.25, cy_pres - 0.08, color="#C0392B")

    _arch_arrow(ax, client[0], cy_pres - 0.36, service[0], cy_svc + 0.39, color="#111")
    _arch_arrow(ax, service[0], cy_svc + 0.31, client[0], cy_pres - 0.44, color="#C0392B")

    _arch_arrow(ax, service[0] - 0.9, cy_svc - 0.39, doc_dao[0] + 0.5, cy_dao + 0.36, color="#111", rad=-0.12)
    _arch_arrow(ax, service[0], cy_svc - 0.39, ref_dao[0], cy_dao + 0.36, color="#111")
    _arch_arrow(ax, doc_dao[0] + 0.5, cy_dao + 0.28, service[0] - 0.9, cy_svc + 0.31, color="#C0392B", rad=-0.12)
    _arch_arrow(ax, ref_dao[0], cy_dao + 0.28, service[0], cy_svc + 0.31, color="#C0392B")

    _arch_arrow(ax, doc_dao[0], cy_dao - 0.36, 4.5, cy_db + 0.46, color="#111", rad=0.08)
    _arch_arrow(ax, ref_dao[0], cy_dao - 0.36, 6.3, cy_db + 0.46, color="#111", rad=-0.08)
    _arch_arrow(ax, 4.5, cy_db + 0.38, doc_dao[0], cy_dao - 0.44, color="#C0392B", rad=0.08)
    _arch_arrow(ax, 6.3, cy_db + 0.38, ref_dao[0], cy_dao - 0.44, color="#C0392B", rad=-0.08)

    ax.text(
        5.0, 7.2,
        "Многоуровневая архитектура программного комплекса",
        ha="center", fontsize=11, weight="bold",
    )
    return _save(fig, "fig_2_2_architecture.png")


def _er_entity(
    ax,
    x: float,
    y: float,
    name: str,
    attrs: list[str],
    *,
    w: float = 2.55,
    color: str = "#E8E8E8",
) -> tuple[float, float, float, float]:
    """Прямоугольник сущности Chen: имя сверху, атрибуты снизу (монохром)."""
    header_h = 0.38
    row_h = 0.22
    body_h = max(0.55, row_h * len(attrs) + 0.12)
    h = header_h + body_h
    ax.add_patch(
        FancyBboxPatch(
            (x, y), w, h, boxstyle="square,pad=0",
            linewidth=1.1, edgecolor="#333", facecolor="white",
        )
    )
    ax.add_patch(
        FancyBboxPatch(
            (x, y + body_h), w, header_h, boxstyle="square,pad=0",
            linewidth=1.1, edgecolor="#333", facecolor=color,
        )
    )
    ax.plot([x, x + w], [y + body_h, y + body_h], color="#333", lw=1.1)
    ax.text(x + w / 2, y + body_h + header_h / 2, name, ha="center", va="center",
            fontsize=8, weight="bold")
    for i, attr in enumerate(attrs):
        ax.text(x + 0.08, y + body_h - 0.18 - i * row_h, attr, ha="left", va="center", fontsize=6.8)
    return x, y, w, h


def _er_link(
    ax,
    x1: float,
    y1: float,
    x2: float,
    y2: float,
    label: str,
    *,
    dashed: bool = False,
    color: str = "#333",
) -> None:
    style = dict(arrowstyle="-|>", lw=1.05, color=color)
    if dashed:
        style["linestyle"] = "dashed"
    ax.annotate(
        label, xy=(x2, y2), xytext=(x1, y1),
        arrowprops=style, fontsize=7, ha="center", color=color,
    )


def draw_er_model() -> str:
    """Логическая ER-модель PostgreSQL (Flyway V1–V3), монохром."""
    fig, ax = plt.subplots(figsize=(14, 6.4))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 6.4)
    ax.axis("off")

    doc = _er_entity(ax, 0.35, 3.55, "document", [
        "PK  id (uuid)",
        "    status",
        "    created_at, updated_at",
    ])
    common = _er_entity(ax, 0.35, 5.55, "document_common_info", [
        "PK/FK  document_id",
        "       tru_name, tru_code",
        "       stage, report_year, plan_year",
    ])
    entry = _er_entity(ax, 3.55, 2.85, "document_entry", [
        "PK  id (uuid)",
        "FK  document_id",
        "FK  section_key",
        "    row_no",
        "    fields (jsonb)",
        "    computed (jsonb)",
        "    validation_status",
    ], w=2.75)
    totals = _er_entity(ax, 3.55, 0.55, "document_totals_snapshot", [
        "PK  id (uuid)",
        "FK  document_id",
        "    totals (jsonb)",
        "    created_at",
    ], w=2.75)
    section = _er_entity(ax, 7.15, 5.55, "dictionary_section_key", [
        "PK  key",
        "    label, form_no",
        "    section_no, version",
        "    active",
    ])
    dvalue = _er_entity(ax, 7.15, 3.55, "dictionary_value", [
        "PK  id (bigserial)",
        "    dictionary_type",
        "    code, label",
        "    version, active",
        "UQ  (type, code, version)",
    ])
    _er_entity(ax, 10.45, 5.55, "reference_material", [
        "PK  id (uuid)",
        "    name",
        "    okpd_code, ekps_code",
        "    fnn",
        "UQ  lower(name)",
    ])
    _er_entity(ax, 10.45, 3.55, "reference_supplier", [
        "PK  id (uuid)",
        "    name, inn",
        "UQ  inn / lower(name)",
    ])

    dx, dy, dw, dh = doc
    cx, cy, cw, ch = common
    ex, ey, ew, eh = entry
    tx, ty, tw, th = totals
    sx, sy, sw, sh = section

    _er_link(ax, dx + dw / 2, dy + dh, cx + cw / 2, cy, "1 : 1")
    _er_link(ax, dx + dw, dy + dh / 2, ex, ey + eh / 2, "1 : N")
    _er_link(ax, dx + dw, dy + dh * 0.25, tx, ty + th, "1 : N")
    _er_link(ax, sx, sy + sh / 2, ex + ew, ey + eh * 0.72, "1 : N")

    return _save(fig, "fig_2_3_er_model.png")


def draw_calculation_flow() -> str:
    fig, ax = plt.subplots(figsize=(10, 4.5))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 4.5)
    ax.axis("off")
    nodes = [
        (0.4, "col7, col9\n(план)"),
        (2.2, "col11 =\ncol7 × col9"),
        (4.0, "col8, col10\n(факт)"),
        (5.8, "col12 =\ncol8 × col10"),
        (7.6, "Агрегация\nпо формам"),
        (9.0, "Итоги\n4/5/6"),
    ]
    for x, label in nodes:
        box = FancyBboxPatch((x, 1.5), 1.4, 1.2, boxstyle="round,pad=0.04",
                             linewidth=1, edgecolor="#333", facecolor="#E8DAEF")
        ax.add_patch(box)
        ax.text(x + 0.7, 2.1, label, ha="center", va="center", fontsize=8)
    for i in range(len(nodes) - 1):
        ax.annotate("", xy=(nodes[i + 1][0], 2.1), xytext=(nodes[i][0] + 1.4, 2.1),
                    arrowprops=dict(arrowstyle="->", lw=1.2))
    ax.text(5, 3.8, "Цепочка расчётов материальных затрат", ha="center", fontsize=11, weight="bold")
    return _save(fig, "fig_2_4_calc_flow.png")


def draw_ui_wizard() -> str:
    fig, ax = plt.subplots(figsize=(10, 3.5))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 3.5)
    ax.axis("off")
    steps = [
        (0.5, "Шаг 1\nОбщие данные\nТРУ, этап, годы"),
        (3.5, "Шаг 2\nУниверсальная строка\nCRUD строк"),
        (6.5, "Шаг 3\nПредпросмотр\nи экспорт"),
    ]
    for i, (x, label) in enumerate(steps):
        color = "#ABEBC6" if i == 1 else "#D5F5E3"
        box = FancyBboxPatch((x, 0.8), 2.5, 1.8, boxstyle="round,pad=0.05",
                             linewidth=1.2, edgecolor="#1E8449", facecolor=color)
        ax.add_patch(box)
        ax.text(x + 1.25, 1.7, label, ha="center", va="center", fontsize=9)
        if i < len(steps) - 1:
            ax.annotate("", xy=(steps[i + 1][0], 1.7), xytext=(x + 2.5, 1.7),
                        arrowprops=dict(arrowstyle="->", lw=1.5, color="#1E8449"))
    ax.text(5, 3.0, "Пользовательский мастер ввода (React SPA)", ha="center", fontsize=11, weight="bold")
    return _save(fig, "fig_3_1_ui_wizard.png")


def draw_export_sequence() -> str:
    fig, ax = plt.subplots(figsize=(11, 5))
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 5)
    ax.axis("off")
    actors = [("Пользователь", 1), ("Frontend", 4), ("Backend", 7), ("PostgreSQL", 10)]
    for name, x in actors:
        ax.plot([x, x], [0.5, 4.5], "k--", lw=0.8)
        ax.text(x, 4.7, name, ha="center", fontsize=9, weight="bold")
    messages = [
        (1.0, 4.0, 4, "POST /export"),
        (4.3, 3.5, 7, "exportDocument()"),
        (7.3, 3.0, 10, "SELECT entries"),
        (7.0, 2.3, 4, "XLSX bytes"),
        (4.0, 1.6, 1, "Скачивание файла"),
    ]
    for y, x1, x2, label in messages:
        style = "->" if x2 > x1 else "<-"
        ax.annotate(label, xy=(x2, y), xytext=(x1, y),
                    arrowprops=dict(arrowstyle=style, lw=1), fontsize=8, ha="center")
    ax.text(5.5, 0.2, "Диаграмма последовательности экспорта XLSX", ha="center", fontsize=11, weight="bold")
    return _save(fig, "fig_3_2_export_sequence.png")


def draw_tech_stack() -> str:
    fig, ax = plt.subplots(figsize=(10, 4))
    ax.axis("off")
    cols = ["Backend", "Frontend", "Данные", "Интеграция"]
    rows = [
        ["Java 21", "React 18", "PostgreSQL 15", "REST JSON"],
        ["Spring Boot 3.3", "TypeScript 5.6", "Flyway", "OpenAPI 3"],
        ["Spring Data JPA", "Vite 5.4", "JSONB", "Apache POI 5.4"],
    ]
    table = ax.table(cellText=rows, colLabels=cols, loc="center", cellLoc="center")
    table.auto_set_font_size(False)
    table.set_fontsize(9)
    table.scale(1.2, 1.8)
    ax.set_title("Стек технологий разработанной системы", fontsize=11, weight="bold", pad=20)
    return _save(fig, "fig_3_3_tech_stack.png")


def generate_chapter2_figures() -> dict[str, str]:
    return {
        "activity": draw_activity_bp01(),
        "architecture": draw_architecture(),
        "er": draw_er_model(),
        "calc": draw_calculation_flow(),
    }


def generate_chapter3_figures() -> dict[str, str]:
    return {
        "wizard": draw_ui_wizard(),
        "export": draw_export_sequence(),
        "stack": draw_tech_stack(),
    }
