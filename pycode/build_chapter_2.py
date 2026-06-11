"""Генерация Главы 2 диплома ВГТУ."""

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
from diploma_figures import generate_chapter2_figures
from diploma_md_loader import load_chapter_sections
from paths import DIPLOMA_MD_DIR, FIGURES_DIR, GENERATED_DIR

GOZ_BPMN_FIGURE = FIGURES_DIR / "fig_2_1_goz_pricing_bpmn.png"
GOZ_FIGURE_REF = "На рисунке 2.1 представлена collaboration-диаграмма"
USE_CASE_REF = "Диаграмма вариантов использования представлена на рисунке 2.3"
ER_MODEL_REF = "ER-модель автоматизации заполнения форм материальных затрат представлена на рисунке ниже"
LOGICAL_MODEL_REF = "Логическая модель базы данных представлена на рисунке ниже"
PHYSICAL_MODEL_REF = "Физическая модель базы данных представлена на рисунке ниже"


def build() -> Path:
    figures = generate_chapter2_figures()
    doc = setup_document()
    add_chapter_title(doc, "2 Анализ и проектирование системы автоматизации")

    sections = load_chapter_sections(
        DIPLOMA_MD_DIR / "chapter_2.md",
        DIPLOMA_MD_DIR / "chapter_2_extra.md",
        DIPLOMA_MD_DIR / "chapter_2_extra2.md",
    )
    fig_counter = 1

    for section in sections:
        add_section_heading(doc, section.heading)
        if section.heading.startswith("2.1"):
            for paragraph in section.paragraphs:
                add_paragraph(doc, paragraph)
                if GOZ_FIGURE_REF in paragraph:
                    add_figure_image(
                        doc,
                        GOZ_BPMN_FIGURE,
                        "Рисунок 2.1 – BPMN-диаграмма бизнес-процесса «Формирование цены договора»",
                    )
                    fig_counter = 2
            add_paragraph(
                doc,
                "На рисунке 2.2 представлена диаграмма деятельности (блок-схема алгоритма) "
                "целевого процесса BP-01 с точками ветвления по результатам валидации, "
                "циклом добавления строк и проверками перед экспортом.",
            )
            add_figure_image(
                doc,
                Path(figures["activity"]),
                "Рисунок 2.2 – Диаграмма деятельности процесса заполнения форм 4/5/6 (BP-01)",
            )
            fig_counter = 3
        elif section.heading.startswith("2.2"):
            for paragraph in section.paragraphs:
                add_paragraph(doc, paragraph)
                if USE_CASE_REF in paragraph:
                    add_figure_placeholder(
                        doc,
                        "ВСТАВИТЬ ДИАГРАММУ: USE CASE СИСТЕМЫ",
                        f"Рисунок 2.{fig_counter} – Диаграмма вариантов использования системы",
                    )
                    fig_counter += 1
        elif section.heading.startswith("2.4"):
            for paragraph in section.paragraphs:
                add_paragraph(doc, paragraph)
                if ER_MODEL_REF in paragraph:
                    add_figure_image(
                        doc,
                        Path(figures["er"]),
                        f"Рисунок 2.{fig_counter} – ER-модель предметной области",
                    )
                    fig_counter += 1
                elif LOGICAL_MODEL_REF in paragraph:
                    add_figure_placeholder(
                        doc,
                        "ВСТАВИТЬ СХЕМУ: ЛОГИЧЕСКАЯ МОДЕЛЬ БД",
                        f"Рисунок 2.{fig_counter} – Логическая модель базы данных",
                    )
                    fig_counter += 1
                elif PHYSICAL_MODEL_REF in paragraph:
                    add_figure_placeholder(
                        doc,
                        "ВСТАВИТЬ СХЕМУ: ФИЗИЧЕСКАЯ МОДЕЛЬ БД",
                        f"Рисунок 2.{fig_counter} – Физическая модель базы данных",
                    )
                    fig_counter += 1
        else:
            for paragraph in section.paragraphs:
                add_paragraph(doc, paragraph)

        if section.heading.startswith("2.1"):
            add_table(
                doc,
                ["Этап as-is", "Проблема", "Решение to-be"],
                [
                    ["Ручной перенос в Excel", "Опечатки, потеря связи план/факт", "Единый ввод в веб-мастере"],
                    ["Разрозненные файлы", "Дублирование и рассинхрон", "Документ-пакет в PostgreSQL"],
                    ["Поиск реквизитов поставщика", "Повторный ввод, ошибки ИНН", "Справочник с автодополнением"],
                    ["Ручное суммирование", "Ошибки в итогах", "GET /totals на сервере"],
                    ["Копирование в шаблон", "Нарушение макета форм", "fill_template через Apache POI"],
                ],
            )
            add_paragraph(
                doc,
                "Дополнительно в проектной документации зафиксирован подпроцесс BP-02, который вызывается "
                "циклически на шаге 2 мастера. Его шлюзы валидации исключают сохранение строки при нарушении "
                "формата чисел или ИНН, что соответствует требованиям снижения «человеческого фактора», "
                "описанным в главе 1.",
            )

        elif section.heading.startswith("2.2"):
            add_table(
                doc,
                ["Подход", "Преимущества", "Ограничения", "Вывод при проектировании"],
                [
                    [
                        "Настольное приложение",
                        "Работа без сети, прямой доступ к файлам",
                        "Нет единой БД, сложное обновление, риск рассинхрона",
                        "Не рекомендуется",
                    ],
                    [
                        "Веб-приложение (тонкий клиент)",
                        "Единый центр логики, кроссплатформенность, совместная работа",
                        "Зависимость от сети и сервера",
                        "Рекомендуется",
                    ],
                    [
                        "Модуль ERP/1С",
                        "Повторное использование учётных справочников",
                        "Длительная интеграция, нет прямой выгрузки в бланк ФАС",
                        "Не рекомендуется",
                    ],
                    [
                        "Толстый клиент",
                        "Меньше обращений к серверу при вводе",
                        "Расхождение расчётов, сложное сопровождение правил",
                        "Не рекомендуется",
                    ],
                ],
            )
            add_paragraph(
                doc,
                "Сравнительный анализ архитектурных альтернатив (таблица выше) обосновывает проектирование "
                "веб-приложения с серверной бизнес-логикой и тонким клиентом для ввода данных.",
            )

        elif section.heading.startswith("2.3"):
            add_paragraph(
                doc,
                f"Структурная схема программного комплекса приведена на рисунке 2.{fig_counter}. "
                "Выделение уровней позволяет независимо развивать интерфейс и серверную логику "
                "при сохранении стабильного контракта REST API.",
            )
            add_figure_image(
                doc,
                Path(figures["architecture"]),
                f"Рисунок 2.{fig_counter} – Многоуровневая архитектура программного комплекса",
            )
            fig_counter += 1
            add_table(
                doc,
                ["Компонент", "Технология", "Роль в системе"],
                [
                    ["core-form-service", "Spring Boot 3.3", "Бизнес-логика, API, экспорт, справочники"],
                    ["core-form-frontend", "React + Vite", "Мастер ввода, автодополнение, справочники"],
                    ["PostgreSQL", "15+", "Хранение документов и справочников"],
                    ["export-template.xlsx", "Apache POI", "Целевой макет форм 4/5/6"],
                ],
            )

        elif section.heading.startswith("2.4"):
            add_paragraph(doc, "Таблица 2 – Названия полей и таблиц логической модели данных")
            add_table(
                doc,
                ["Физическая модель", "Логическая модель", "Описание"],
                [
                    ["document", "Документ-пакет", "Корневой объект хранения"],
                    ["id (PK)", "—", "Уникальный идентификатор"],
                    ["status", "Статус", "Черновик или завершённый пакет"],
                    ["created_at", "Дата создания", "Момент создания документа"],
                    ["updated_at", "Дата изменения", "Момент последнего изменения"],
                    ["document_common_info", "Общие сведения по ТРУ", "Блок шапки форм"],
                    ["document_id (PK, FK)", "Документ", "Связь один к одному с документом"],
                    ["tru_name", "Наименование ТРУ", "Наименование товарно-работы-услуги"],
                    ["tru_code", "Шифр", "Шифр изделия или работы"],
                    ["stage", "Этап", "Этап выполнения"],
                    ["report_year", "Отчётный год", "Год отчётности"],
                    ["plan_year", "Плановый год", "Плановый год"],
                    ["document_entry", "Строка затрат", "Строка материальных показателей"],
                    ["id (PK)", "—", "Уникальный идентификатор строки"],
                    ["document_id (FK)", "Документ", "Принадлежность пакету"],
                    ["section_key (FK)", "Раздел", "Связь со справочником разделов"],
                    ["row_no", "Номер строки", "Порядковый номер в разделе"],
                    ["fields", "Вводимые графы", "JSON-атрибуты col2–col15"],
                    ["computed", "Расчётные графы", "JSON-атрибуты col11, col12"],
                    ["validation_status", "Статус проверки", "VALID, INVALID или WARNING"],
                    ["dictionary_section_key", "Справочник разделов", "Разделы форм 4/5/6"],
                    ["key (PK)", "Ключ раздела", "Системный идентификатор"],
                    ["form_no", "Номер формы", "4, 5 или 6"],
                    ["section_no", "Номер раздела", "Порядковый номер в форме"],
                    ["reference_material", "Материал", "Справочник номенклатуры"],
                    ["name", "Наименование", "Наименование материала"],
                    ["okpd_code", "Код ОКПД", "Код по ОКПД"],
                    ["ekps_code", "Код ЕКПС", "Код по ЕКПС"],
                    ["fnn", "Номер ФНН", "Номер по ФНН"],
                    ["reference_supplier", "Поставщик", "Справочник организаций"],
                    ["inn", "ИНН", "Идентификационный номер налогоплательщика"],
                ],
            )
            add_paragraph(
                doc,
                "Таблица 2 связывает концептуальные сущности ER-модели с проектируемыми таблицами "
                "реляционной базы данных. Центральной остаётся сущность «Документ-пакет», к которой "
                "относится один блок общих сведений и множество строк затрат.",
            )

    output = GENERATED_DIR / "Глава_2_Методы.docx"
    try:
        return save_document(doc, output)
    except PermissionError:
        alt = GENERATED_DIR / "Глава_2_Методы_new.docx"
        return save_document(doc, alt)


if __name__ == "__main__":
    path = build()
    print(f"Собрано: {path}")
