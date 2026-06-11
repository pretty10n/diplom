"""Генерация Главы 3 диплома ВГТУ."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from diploma_doc_builder import (
    add_chapter_title,
    add_figure_image,
    add_figure_placeholder,
    add_paragraph,
    add_section_heading,
    add_table,
    save_document,
    setup_document,
)
from diploma_figures import generate_chapter3_figures
from diploma_md_loader import load_chapter_sections
from paths import DIPLOMA_MD_DIR, GENERATED_DIR


def build() -> Path:
    figures = generate_chapter3_figures()
    doc = setup_document()
    add_chapter_title(doc, "3 Разработка и реализация системы автоматизированного заполнения документов")

    sections = load_chapter_sections(
        DIPLOMA_MD_DIR / "chapter_3.md",
        DIPLOMA_MD_DIR / "chapter_3_extra.md",
        DIPLOMA_MD_DIR / "chapter_3_extra2.md",
    )
    fig_counter = 1

    for section in sections:
        add_section_heading(doc, section.heading)
        for paragraph in section.paragraphs:
            add_paragraph(doc, paragraph)

        if section.heading.startswith("3.1"):
            add_figure_image(
                doc,
                Path(figures["stack"]),
                f"Рисунок 3.{fig_counter} – Стек технологий разработанной системы",
            )
            fig_counter += 1
            add_table(
                doc,
                ["Критерий выбора", "Java/Spring", "React/TS", "PostgreSQL", "Apache POI"],
                [
                    ["Зрелость экосистемы", "высокая", "высокая", "высокая", "средняя"],
                    ["Типизация расчётов", "BigDecimal", "на клиенте preview", "CHECK/JSONB", "—"],
                    ["Скорость разработки UI", "—", "высокая", "—", "—"],
                    ["Сохранение макета Excel", "—", "—", "—", "да"],
                ],
            )
            add_paragraph(
                doc,
                "Сравнительный анализ альтернатив (чистый Node.js backend, генерация DOCX вместо XLSX, "
                "хранение строк в отдельных колонках реляционной таблицы) показал, что выбранный стек "
                "обеспечивает наилучший баланс между соответствием регламенту Excel и скоростью итераций MVP.",
            )

        elif section.heading.startswith("3.2"):
            add_figure_image(
                doc,
                Path(figures["wizard"]),
                f"Рисунок 3.{fig_counter} – Пользовательский мастер ввода данных",
            )
            fig_counter += 1
            add_table(
                doc,
                ["Шаг", "Действие пользователя", "API", "Таблицы БД"],
                [
                    ["1", "Ввод общих данных ТРУ", "POST/PATCH /documents", "document, document_common_info"],
                    ["2", "Добавление строки", "POST /entries", "document_entry, reference_*"],
                    ["2", "Автозаполнение материала/поставщика", "GET .../reference-data/.../search", "reference_material, reference_supplier"],
                    ["2", "Управление справочниками", "POST/PATCH/DELETE /reference-data", "reference_material, reference_supplier"],
                    ["2", "Редактирование/удаление", "PATCH/DELETE /entries", "document_entry"],
                    ["3", "Предпросмотр", "GET /entries, GET /totals", "агрегация в сервисе"],
                    ["3", "Экспорт", "POST /export, GET /files", "export-template.xlsx"],
                ],
            )
            add_paragraph(
                doc,
                "Взаимодействие компонентов при экспорте иллюстрируется диаграммой последовательности "
                "на рисунке 3.2. Запрос на формирование файла инициируется только после актуализации "
                "предпросмотра, что предотвращает выгрузку устаревших данных.",
            )
            add_figure_image(
                doc,
                Path(figures["export"]),
                f"Рисунок 3.{fig_counter} – Диаграмма последовательности экспорта XLSX",
            )
            fig_counter += 1
            add_figure_placeholder(
                doc,
                "ВСТАВИТЬ СКРИНШОТ: ШАГ 2 МАСТЕРА — АВТОЗАПОЛНЕНИЕ И СПРАВОЧНИКИ",
                f"Рисунок 3.{fig_counter} – Интерфейс автозаполнения и окна «Справочники» (шаг 2 мастера)",
            )
            fig_counter += 1
            add_paragraph(
                doc,
                "Ключевые классы реализации backend: DocumentController (работа с документами), "
                "ReferenceDataController (справочники), DocumentApiService (транзакции, валидация, "
                "syncFromEntryFields), ReferenceDataService (CRUD и поиск), XlsxTemplateExportService "
                "(заполнение шаблона), EntryRowNumberService (нумерация). Frontend реализует типизированный "
                "клиент api/client.ts, компоненты AutocompleteInput и ReferenceDataManager, а также "
                "единый компонент App.tsx с управлением состоянием шагов и таблицей строк.",
            )

        elif section.heading.startswith("3.3"):
            add_table(
                doc,
                ["Категория", "Минимум", "Рекомендуется"],
                [
                    ["Сервер CPU", "2 ядра", "4 ядра"],
                    ["Сервер RAM", "4 ГБ", "8 ГБ"],
                    ["Диск", "10 ГБ", "50 ГБ SSD"],
                    ["СУБД", "PostgreSQL 15", "PostgreSQL 16"],
                    ["Браузер", "Chromium 120+", "актуальный LTS"],
                    ["Сеть", "100 Мбит/с", "1 Гбит/с в ЛВС"],
                ],
            )
            add_paragraph(
                doc,
                "Для локальной разработки достаточно JDK 21, Maven 3.9+, Node.js 20+ и экземпляра PostgreSQL "
                "на localhost:5432. Команды запуска: mvn spring-boot:run в каталоге core-form-service и "
                "npm run dev в каталоге frontend. Прокси Vite перенаправляет /api на порт 8080, что исключает "
                "проблемы CORS в dev-режиме.",
            )
            add_paragraph(
                doc,
                "Перспективы развития системы включают: подключение аутентификации и аудита действий; "
                "поддержку форм 1, 2, 7 и 20; активацию разделов возвратных отходов; автосохранение черновика; "
                "импорт справочников из внешних систем учёта; версионирование шаблонов Excel при изменении "
                "приказа ФАС. Данные направления зафиксированы как открытые вопросы в реестре ТЗ и не входят "
                "в scope текущего MVP.",
            )

    output = GENERATED_DIR / "Глава_3_Реализация.docx"
    try:
        return save_document(doc, output)
    except PermissionError:
        alt = GENERATED_DIR / "Глава_3_Реализация_new.docx"
        return save_document(doc, alt)


if __name__ == "__main__":
    path = build()
    print(f"Собрано: {path}")
